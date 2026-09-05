package com.example

import android.app.Application
import androidx.room.Room
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.data.local.AppDatabase
import com.example.data.repository.TownshipRepository

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
        .addMigrations(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8,
            AppDatabase.MIGRATION_8_9
        )
        .fallbackToDestructiveMigration(true)
        .fallbackToDestructiveMigrationOnDowngrade(true)
        .build()
    }

    val repository: TownshipRepository by lazy {
        TownshipRepository(database.appDao(), applicationContext)
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
