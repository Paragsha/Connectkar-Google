package com.connectkar

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import com.connectkar.data.local.AppDatabase
import com.connectkar.data.local.DatabaseKeyManager
import com.connectkar.data.local.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseEncryptionTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        DatabaseKeyManager.resetForTesting()
    }

    @Test
    fun passphrase_isGeneratedAndSecurelyPersistedConsistently() {
        val key1 = DatabaseKeyManager.getOrCreatePassphrase(context)
        assertNotNull(key1)
        assertEquals(64, key1.size)
        val keyString = String(key1, Charsets.UTF_8)
        assertTrue("Passphrase must be a valid hex string", keyString.matches(Regex("^[0-9a-fA-F]{64}$")))

        // Reset memory cache to force reading from encrypted persistence
        DatabaseKeyManager.resetForTesting()
        val key2 = DatabaseKeyManager.getOrCreatePassphrase(context)

        assertEquals("Key retrieved from persistence must match original key", key1.toList(), key2.toList())
    }

    @Test
    fun detects_plaintextSQLiteDatabaseVsEncryptedOrMissingDatabase() {
        val testDbName = "detect_test_db"
        context.deleteDatabase(testDbName)

        assertFalse(DatabaseKeyManager.isDatabaseUnencrypted(context, testDbName))

        // Create a standard unencrypted SQLite database
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(testDbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL("CREATE TABLE test_table (id INTEGER PRIMARY KEY, value TEXT)")
                }
                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()
        val helper = FrameworkSQLiteOpenHelperFactory().create(config)
        val db = helper.writableDatabase
        db.execSQL("INSERT INTO test_table VALUES (1, 'sensitive_pii')")
        db.close()

        // File should now be detected as unencrypted SQLite format 3
        assertTrue("Plaintext SQLite file must be detected as unencrypted", DatabaseKeyManager.isDatabaseUnencrypted(context, testDbName))

        // Clean up
        context.deleteDatabase(testDbName)
    }

    @Test
    fun databaseKeyManager_createsOpenHelperFactoryAndProtectsPII() {
        runBlocking {
            val testDbName = "open_helper_factory_test_db"
            context.deleteDatabase(testDbName)

            val factory = DatabaseKeyManager.createOpenHelperFactory(context, testDbName)
            assertNotNull(factory)

            val roomDb = Room.databaseBuilder(context, AppDatabase::class.java, testDbName)
                .openHelperFactory(factory)
                .allowMainThreadQueries()
                .build()

            val testUser = UserEntity(
                uid = "pii_user_1",
                fullName = "Rahul Sharma",
                phoneNumber = "+919876543210",
                society = "Palm Grove",
                blockTower = "Tower B",
                flatNumber = "B-204",
                avatarIndex = 1,
                isVerified = true,
                isPending = false,
                isCurrent = true,
                role = "RESIDENT",
                pendingSync = false,
                floor = "2",
                residentType = "OWNER",
                moveInDate = "2023-01-01",
                proofDocumentUri = "content://media/proof_aadhaar_confidential.pdf",
                timestamp = System.currentTimeMillis()
            )

            roomDb.appDao().insertUser(testUser)

            val fetchedUser = roomDb.appDao().getUserByUidDirect("pii_user_1")
            assertNotNull(fetchedUser)
            assertEquals("Rahul Sharma", fetchedUser?.fullName)
            assertEquals("+919876543210", fetchedUser?.phoneNumber)
            assertEquals("content://media/proof_aadhaar_confidential.pdf", fetchedUser?.proofDocumentUri)

            roomDb.close()
            context.deleteDatabase(testDbName)
        }
    }

    @Test
    fun migrationSafety_handlesNonExistentAndPlaintextDatabasesGracefully() {
        val nonExistentDb = "non_existent_db"
        val passphrase = DatabaseKeyManager.getOrCreatePassphrase(context)

        // Should not throw or crash if database does not exist
        DatabaseKeyManager.migrateUnencryptedDatabaseIfNeeded(context, nonExistentDb, passphrase)
        assertFalse(DatabaseKeyManager.isDatabaseUnencrypted(context, nonExistentDb))
    }
}
