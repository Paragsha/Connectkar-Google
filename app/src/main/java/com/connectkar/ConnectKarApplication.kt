package com.connectkar

import android.app.Application
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import com.connectkar.data.local.AppDatabase
import com.connectkar.data.repository.TownshipRepository

class ConnectKarApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "connectkar_db"
        )
        .openHelperFactory(com.connectkar.data.local.DatabaseKeyManager.createOpenHelperFactory(applicationContext, "connectkar_db"))
        // CRITICAL: Any future schema version bump must include a corresponding MIGRATION_N_N+1
        // before merging. Precedent: A missing MIGRATION_5_6 caused .fallbackToDestructiveMigration()
        // to silently wipe all local UserEntity and ListingEntity tables during app upgrades from v5 to v6.
        .addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9,
            AppDatabase.MIGRATION_9_10,
            AppDatabase.MIGRATION_10_11,
            AppDatabase.MIGRATION_11_12
        )
        .build()
    }

    val authRepository: com.connectkar.data.repository.AuthRepository by lazy {
        com.connectkar.data.repository.FirebaseAuthRepository.getInstance(applicationContext)
    }

    val repository: TownshipRepository by lazy {
        TownshipRepository(database.appDao(), applicationContext, authRepository = authRepository)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        try {
            // Configuration.Provider initializes WorkManager on demand or via AndroidX Startup
            WorkManager.getInstance(this)
        } catch (t: Throwable) {
            android.util.Log.w("ConnectKarApplication", "WorkManager startup check: ${t.message}")
        }
    }

    companion object {
        lateinit var instance: ConnectKarApplication
            private set
    }
}
