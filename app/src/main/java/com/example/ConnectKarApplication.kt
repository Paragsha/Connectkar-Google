package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.TownshipRepository

class ConnectKarApplication : Application() {

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
            AppDatabase.MIGRATION_6_7
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    val repository: TownshipRepository by lazy {
        TownshipRepository(database.appDao(), applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: ConnectKarApplication
            private set
    }
}
