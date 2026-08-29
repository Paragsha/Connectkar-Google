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
        val app = applicationContext as com.example.ConnectKarApplication
        val appDao = app.database.appDao()
        val firestore = FirebaseManager.firestore

        if (runAttemptCount > 3) {
            android.util.Log.e("SyncWorker", "Max retry limit exceeded. Sync aborted.")
            return Result.failure()
        }

        if (firestore == null) {
            android.util.Log.w("SyncWorker", "Firestore is unavailable. Retrying sync.")
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

            // 3. Sync Unsynced Chef Profiles
            val unsyncedChefs = appDao.getUnsyncedChefProfiles()
            for (chef in unsyncedChefs) {
                if (chef.uid.isNotEmpty()) {
                    firestore.collection("chefProfiles").document(chef.uid).set(chef.toFirestoreMap()).await()
                    appDao.updateChefProfile(chef.copy(pendingSync = false))
                }
            }

            // 4. Sync Unsynced Menu Items
            val unsyncedMenuItems = appDao.getUnsyncedMenuItems()
            for (item in unsyncedMenuItems) {
                val docRef = if (item.firestoreId.isNotEmpty() && !item.firestoreId.startsWith("local_")) {
                    firestore.collection("menuItems").document(item.firestoreId)
                } else {
                    firestore.collection("menuItems").document()
                }
                val finalItem = item.copy(firestoreId = docRef.id, pendingSync = false)
                docRef.set(finalItem.toFirestoreMap()).await()
                appDao.updateMenuItem(finalItem)
            }

            // 5. Sync Unsynced Meal Orders
            val unsyncedOrders = appDao.getUnsyncedMealOrders()
            for (order in unsyncedOrders) {
                val docRef = if (order.firestoreId.isNotEmpty() && !order.firestoreId.startsWith("local_")) {
                    firestore.collection("mealOrders").document(order.firestoreId)
                } else {
                    firestore.collection("mealOrders").document()
                }
                val finalOrder = order.copy(firestoreId = docRef.id, pendingSync = false)
                docRef.set(finalOrder.toFirestoreMap()).await()
                appDao.updateMealOrder(finalOrder)
            }

            // 6. Sync Unsynced Meal Subscriptions
            val unsyncedSubs = appDao.getUnsyncedMealSubscriptions()
            for (sub in unsyncedSubs) {
                val docRef = if (sub.firestoreId.isNotEmpty() && !sub.firestoreId.startsWith("local_")) {
                    firestore.collection("mealSubscriptions").document(sub.firestoreId)
                } else {
                    firestore.collection("mealSubscriptions").document()
                }
                val finalSub = sub.copy(firestoreId = docRef.id, pendingSync = false)
                docRef.set(finalSub.toFirestoreMap()).await()
                appDao.updateMealSubscription(finalSub)
            }

            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Sync failed: ${e.message}", e)
            return Result.retry()
        }
    }
}
