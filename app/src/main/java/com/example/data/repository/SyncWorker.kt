package com.example.data.repository

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.FirebaseManager
import com.example.data.local.AppDatabase
import com.example.data.local.toFirestoreMap
import androidx.room.Room
import com.example.data.await

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "connectkar_db"
        )
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
        .fallbackToDestructiveMigration()
        .build()

        val appDao = database.appDao()
        val firestore = FirebaseManager.firestore

        if (firestore == null) {
            return Result.retry()
        }

        try {
            // 1. Sync Unsynced Users
            val unsyncedUsers = appDao.getUnsyncedUsers()
            for (user in unsyncedUsers) {
                if (user.uid.isNotEmpty()) {
                    val userMap = user.toFirestoreMap()
                    firestore.collection("users").document(user.uid).set(userMap).await()
                    appDao.updateUser(user.copy(pendingSync = false))
                }
            }

            // 2. Sync Unsynced Listings
            val unsyncedListings = appDao.getUnsyncedListings()
            for (listing in unsyncedListings) {
                val docRef = if (listing.firestoreId.isNotEmpty() && !listing.firestoreId.startsWith("local_")) {
                    firestore.collection("listings").document(listing.firestoreId)
                } else {
                    firestore.collection("listings").document()
                }
                
                val finalListing = listing.copy(firestoreId = docRef.id, pendingSync = false)
                val listingMap = finalListing.toFirestoreMap()
                
                docRef.set(listingMap).await()
                appDao.updateListing(finalListing)
            }

            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Sync failed: ${e.message}", e)
            return Result.retry()
        }
    }
}
