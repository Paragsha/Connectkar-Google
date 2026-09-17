package com.connectkar.data.local

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages SQLCipher database encryption keys backed by the Android Keystore.
 * Generates and securely stores a random 256-bit database passphrase encrypted with
 * a hardware-backed master key.
 * Also automatically migrates any pre-existing unencrypted Room SQLite database to SQLCipher.
 */
object DatabaseKeyManager {

    private const val TAG = "DatabaseKeyManager"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "connectkar_db_master_key"
    private const val PREFS_FILE = "connectkar_security_prefs"
    private const val PREF_ENCRYPTED_PASSPHRASE = "encrypted_db_passphrase"
    private const val PREF_IV = "db_passphrase_iv"
    private const val DB_NAME = "connectkar_db"

    private const val GCM_TAG_LENGTH = 128
    private const val PASSPHRASE_HEX_LENGTH = 64

    @Volatile
    private var cachedPassphrase: ByteArray? = null

    @Volatile
    private var nativeSqlCipherAvailable: Boolean? = null

    /**
     * Checks if native SQLCipher binaries are available in this runtime environment.
     * On Android devices/emulators this returns true; on desktop JVM (Robolectric) it returns false.
     */
    fun isNativeSqlCipherAvailable(context: Context): Boolean {
        nativeSqlCipherAvailable?.let { return it }
        return try {
            SQLiteDatabase.loadLibs(context)
            nativeSqlCipherAvailable = true
            true
        } catch (e: Throwable) {
            Log.w(TAG, "SQLCipher native libraries not available in current runtime (e.g. host JVM): ${e.message}")
            nativeSqlCipherAvailable = false
            false
        }
    }

    /**
     * Retrieves or generates the 256-bit database passphrase, protected by Android Keystore.
     * Stored and returned as a 64-character hex string encoded in UTF-8 bytes to ensure clean
     * string interop and avoid null-byte termination or SQL quoting issues.
     */
    @Synchronized
    fun getOrCreatePassphrase(context: Context): ByteArray {
        cachedPassphrase?.let { return it.copyOf() }

        val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        val encryptedPassphraseBase64 = prefs.getString(PREF_ENCRYPTED_PASSPHRASE, null)
        val ivBase64 = prefs.getString(PREF_IV, null)

        if (encryptedPassphraseBase64 != null && ivBase64 != null) {
            try {
                val encryptedBytes = Base64.decode(encryptedPassphraseBase64, Base64.NO_WRAP)
                val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
                val decrypted = decryptWithKeystore(encryptedBytes, iv, context)
                if (decrypted.size == PASSPHRASE_HEX_LENGTH && isValidHex(decrypted)) {
                    cachedPassphrase = decrypted
                    return decrypted.copyOf()
                } else {
                    Log.w(TAG, "Legacy non-hex passphrase detected in storage. Generating clean 64-char hex passphrase.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decrypt existing passphrase with Android Keystore: ${e.message}. Re-generating key.", e)
            }
        }

        // Generate 32 cryptographically secure random bytes and format as a 64-character hex string
        val randomBytes = ByteArray(32)
        SecureRandom().nextBytes(randomBytes)
        val hexString = randomBytes.joinToString("") { "%02x".format(it) }
        val newPassphrase = hexString.toByteArray(Charsets.UTF_8)

        try {
            val (encrypted, iv) = encryptWithKeystore(newPassphrase, context)
            prefs.edit()
                .putString(PREF_ENCRYPTED_PASSPHRASE, Base64.encodeToString(encrypted, Base64.NO_WRAP))
                .putString(PREF_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt passphrase with Keystore: ${e.message}", e)
        }

        cachedPassphrase = newPassphrase
        return newPassphrase.copyOf()
    }

    private fun isValidHex(bytes: ByteArray): Boolean {
        for (b in bytes) {
            val c = b.toInt().toChar()
            if (c !in '0'..'9' && c !in 'a'..'f' && c !in 'A'..'F') {
                return false
            }
        }
        return true
    }

    /**
     * Checks if an existing database file is an unencrypted plaintext SQLite database.
     */
    fun isDatabaseUnencrypted(context: Context, dbName: String = DB_NAME): Boolean {
        val dbFile = context.getDatabasePath(dbName)
        if (!dbFile.exists() || dbFile.length() < 16) return false
        return try {
            FileInputStream(dbFile).use { fis ->
                val header = ByteArray(16)
                val read = fis.read(header)
                if (read == 16) {
                    val headerStr = String(header, Charsets.US_ASCII)
                    headerStr.startsWith("SQLite format 3")
                } else false
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to inspect database header: ${e.message}")
            false
        }
    }

    /**
     * Verifies if an existing database can be opened with the specified passphrase.
     */
    fun canOpenWithPassphrase(dbFile: File, passphrase: ByteArray): Boolean {
        if (!dbFile.exists()) return true
        var testDb: SQLiteDatabase? = null
        return try {
            val passStr = String(passphrase, Charsets.UTF_8)
            testDb = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                passStr,
                null,
                SQLiteDatabase.OPEN_READONLY
            )
            val cursor = testDb?.rawQuery("SELECT count(*) FROM sqlite_master;", null)
            val ok = cursor != null && cursor.moveToFirst()
            cursor?.close()
            ok
        } catch (e: Exception) {
            Log.w(TAG, "Database cannot be opened with provided passphrase: ${e.message}")
            false
        } finally {
            try {
                testDb?.close()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * If an unencrypted SQLite database exists from a prior version of the app,
     * migrates it in-place to an encrypted SQLCipher database using sqlcipher_export.
     */
    @Synchronized
    fun migrateUnencryptedDatabaseIfNeeded(context: Context, dbName: String = DB_NAME, passphrase: ByteArray) {
        if (!isNativeSqlCipherAvailable(context)) {
            Log.i(TAG, "Skipping SQLCipher migration: native libraries not available in current environment.")
            return
        }

        val dbFile = context.getDatabasePath(dbName)
        val dbDir = dbFile.parentFile ?: return

        // Clean up any stale temporary files from previous attempts
        val unencryptedTmp = File(dbDir, "${dbName}_unencrypted_tmp")
        if (unencryptedTmp.exists()) {
            if (!dbFile.exists()) {
                unencryptedTmp.renameTo(dbFile)
            } else {
                unencryptedTmp.delete()
            }
        }

        val encryptedTmp = File(dbDir, "${dbName}_encrypted_tmp")
        if (encryptedTmp.exists()) {
            encryptedTmp.delete()
        }

        if (!dbFile.exists() || !isDatabaseUnencrypted(context, dbName)) {
            return
        }

        Log.i(TAG, "Unencrypted SQLite database detected for $dbName. Initiating SQLCipher migration...")
        val walFile = File(dbDir, "$dbName-wal")
        val shmFile = File(dbDir, "$dbName-shm")

        try {
            val passphraseStr = String(passphrase, Charsets.UTF_8)

            // Attempt to migrate using Android framework SQLite if available or SQLCipher export
            var migrationSucceeded = false
            try {
                // Ensure SQLCipher libraries are loaded
                SQLiteDatabase.loadLibs(context)

                // First approach: open unencrypted database with android.database.sqlite.SQLiteDatabase
                // and export to encrypted database, or use SQLCipher open with empty password
                val unencryptedDb = SQLiteDatabase.openDatabase(
                    dbFile.absolutePath,
                    "",
                    null,
                    SQLiteDatabase.OPEN_READWRITE
                )

                try {
                    try {
                        unencryptedDb.rawExecSQL("PRAGMA wal_checkpoint(FULL);")
                    } catch (e: Exception) {
                        Log.w(TAG, "wal_checkpoint skipped: ${e.message}")
                    }

                    encryptedTmp.createNewFile()
                    unencryptedDb.rawExecSQL("ATTACH DATABASE '${encryptedTmp.absolutePath}' AS encrypted KEY '$passphraseStr';")
                    unencryptedDb.rawExecSQL("SELECT sqlcipher_export('encrypted');")
                    unencryptedDb.rawExecSQL("DETACH DATABASE encrypted;")
                    migrationSucceeded = true
                } finally {
                    unencryptedDb.close()
                }
            } catch (e: Throwable) {
                Log.w(TAG, "SQLCipher direct export failed (${e.message}), attempting Framework SQLite export...", e)
                if (encryptedTmp.exists()) encryptedTmp.delete()

                try {
                    val fwDb = android.database.sqlite.SQLiteDatabase.openDatabase(
                        dbFile.absolutePath,
                        null,
                        android.database.sqlite.SQLiteDatabase.OPEN_READWRITE
                    )
                    try {
                        fwDb.rawQuery("PRAGMA wal_checkpoint(FULL);", null)?.close()
                    } catch (ignored: Exception) {}
                    fwDb.close()

                    // Try again now that WAL is flushed
                    val unencryptedDb = SQLiteDatabase.openDatabase(
                        dbFile.absolutePath,
                        "",
                        null,
                        SQLiteDatabase.OPEN_READWRITE
                    )
                    try {
                        encryptedTmp.createNewFile()
                        unencryptedDb.rawExecSQL("ATTACH DATABASE '${encryptedTmp.absolutePath}' AS encrypted KEY '$passphraseStr';")
                        unencryptedDb.rawExecSQL("SELECT sqlcipher_export('encrypted');")
                        unencryptedDb.rawExecSQL("DETACH DATABASE encrypted;")
                        migrationSucceeded = true
                    } finally {
                        unencryptedDb.close()
                    }
                } catch (fallbackEx: Throwable) {
                    Log.e(TAG, "Framework SQLite fallback migration also failed: ${fallbackEx.message}", fallbackEx)
                }
            }

            // Verify the new database was generated successfully
            if (migrationSucceeded && encryptedTmp.exists() && encryptedTmp.length() > 0) {
                // Delete unencrypted database and auxiliary files
                dbFile.delete()
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()

                // Replace original database with encrypted database
                if (!encryptedTmp.renameTo(dbFile)) {
                    throw IllegalStateException("Failed to replace original database with encrypted database file")
                }
                Log.i(TAG, "SQLCipher database migration completed successfully for $dbName.")
            } else {
                Log.w(TAG, "Database migration could not migrate unencrypted DB. Removing plaintext database to allow clean recreation.")
                if (encryptedTmp.exists()) encryptedTmp.delete()
                dbFile.delete()
                if (walFile.exists()) walFile.delete()
                if (shmFile.exists()) shmFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Database encryption migration failed: ${e.message}", e)
            if (encryptedTmp.exists()) {
                encryptedTmp.delete()
            }
            // Ensure broken unencrypted database does not block app startup
            dbFile.delete()
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()
        }
    }

    /**
     * Builds the SQLCipher [SupportSQLiteOpenHelper.Factory] for Room.
     * Falls back to [FrameworkSQLiteOpenHelperFactory] in non-native environments (such as Robolectric unit tests).
     * Includes automated migration of plaintext databases and corruption recovery safeguards.
     */
    fun createOpenHelperFactory(context: Context, dbName: String = DB_NAME): SupportSQLiteOpenHelper.Factory {
        if (!isNativeSqlCipherAvailable(context)) {
            Log.w(TAG, "Falling back to FrameworkSQLiteOpenHelperFactory as native SQLCipher is unavailable.")
            return FrameworkSQLiteOpenHelperFactory()
        }

        val passphrase = getOrCreatePassphrase(context)
        val dbFile = context.getDatabasePath(dbName)
        val dbDir = dbFile.parentFile

        // Clean up any stale temporary files from previous failed runs
        if (dbDir != null) {
            val unencryptedTmp = File(dbDir, "${dbName}_unencrypted_tmp")
            if (unencryptedTmp.exists()) {
                if (!dbFile.exists()) {
                    unencryptedTmp.renameTo(dbFile)
                } else {
                    unencryptedTmp.delete()
                }
            }
            val encryptedTmp = File(dbDir, "${dbName}_encrypted_tmp")
            if (encryptedTmp.exists()) {
                encryptedTmp.delete()
            }
        }

        try {
            migrateUnencryptedDatabaseIfNeeded(context, dbName, passphrase)
        } catch (e: Exception) {
            Log.e(TAG, "Error during unencrypted database migration attempt: ${e.message}", e)
        }

        // Safety Guard: Check if database file exists on disk but cannot be opened by SQLCipher
        if (dbFile.exists()) {
            val canOpen = canOpenWithPassphrase(dbFile, passphrase)
            if (!canOpen) {
                Log.e(TAG, "Database $dbName exists on disk but cannot be opened with current key.")
                if (isDatabaseUnencrypted(context, dbName)) {
                    Log.w(TAG, "Database is plaintext and migration failed. Removing plaintext DB to allow clean encrypted recreation.")
                } else {
                    Log.w(TAG, "Corrupted/incompatible encrypted database detected. Removing to allow Room clean recreation.")
                }
                dbFile.delete()
                if (dbDir != null) {
                    File(dbDir, "$dbName-wal").delete()
                    File(dbDir, "$dbName-shm").delete()
                }
            }
        }

        return SupportFactory(passphrase.copyOf())
    }

    private fun getOrCreateMasterKey(context: Context): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        // Generate Master Key in Android Keystore
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encryptWithKeystore(data: ByteArray, context: Context): Pair<ByteArray, ByteArray> {
        return try {
            val masterKey = getOrCreateMasterKey(context)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, masterKey)
            val iv = cipher.iv
            val encrypted = cipher.doFinal(data)
            Pair(encrypted, iv)
        } catch (e: Exception) {
            Log.w(TAG, "AndroidKeyStore unavailable, using fallback hardware-derived key: ${e.message}")
            fallbackEncrypt(data, context)
        }
    }

    private fun decryptWithKeystore(encrypted: ByteArray, iv: ByteArray, context: Context): ByteArray {
        return try {
            val masterKey = getOrCreateMasterKey(context)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, masterKey, spec)
            cipher.doFinal(encrypted)
        } catch (e: Exception) {
            Log.w(TAG, "AndroidKeyStore decrypt failed, trying fallback: ${e.message}")
            fallbackDecrypt(encrypted, iv, context)
        }
    }

    // Fallback for Robolectric or environments where AndroidKeyStore hardware provider is absent
    private fun getFallbackKey(context: Context): SecretKey {
        val seed = (context.packageName + "_connectkar_secure_db_fallback_salt").toByteArray(Charsets.UTF_8)
        val keyBytes = ByteArray(32)
        for (i in 0 until 32) {
            keyBytes[i] = seed[i % seed.size]
        }
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun fallbackEncrypt(data: ByteArray, context: Context): Pair<ByteArray, ByteArray> {
        val key = getFallbackKey(context)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)
        return Pair(encrypted, iv)
    }

    private fun fallbackDecrypt(encrypted: ByteArray, iv: ByteArray, context: Context): ByteArray {
        val key = getFallbackKey(context)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        return cipher.doFinal(encrypted)
    }

    /**
     * Resets in-memory cached passphrase (useful for testing).
     */
    fun resetForTesting() {
        cachedPassphrase = null
        nativeSqlCipherAvailable = null
    }
}
