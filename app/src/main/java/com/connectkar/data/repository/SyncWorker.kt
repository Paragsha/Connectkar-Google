package com.connectkar.data.repository

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.connectkar.data.FirebaseManager
import com.connectkar.data.local.AppDatabase
import com.connectkar.data.local.toFirestoreMap
import androidx.room.Room
import com.connectkar.data.await

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val UNIQUE_WORK_NAME = "ConnectKarSyncWork"

        fun enqueueSync(context: Context, initialDelaySeconds: Long = 0) {
            try {
                val constraints = androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()

                val builder = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(
                        androidx.work.BackoffPolicy.EXPONENTIAL,
                        androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                        java.util.concurrent.TimeUnit.MILLISECONDS
                    )

                if (initialDelaySeconds > 0) {
                    builder.setInitialDelay(initialDelaySeconds, java.util.concurrent.TimeUnit.SECONDS)
                }

                val syncRequest = builder.build()

                androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
            } catch (e: Exception) {
                android.util.Log.e("SyncWorker", "Failed to enqueue sync job: ${e.message}")
            }
        }
    }

    override suspend fun doWork(): Result {
        val app = applicationContext as com.connectkar.ConnectKarApplication
        val appDao = app.database.appDao()
        val firestore = FirebaseManager.firestore

        if (runAttemptCount > 3) {
            android.util.Log.e("SyncWorker", "Max retry limit exceeded for current request. Re-enqueueing fresh sync job to prevent orphaned rows.")
            enqueueSync(applicationContext, initialDelaySeconds = 30)
            return Result.failure()
        }

        if (firestore == null) {
            android.util.Log.w("SyncWorker", "Firestore is unavailable. Retrying sync.")
            return Result.retry()
        }

        var hasFailure = false

        // 1. Sync Unsynced Users & Pending Verifications
        try {
            val unsyncedUsers = appDao.getUnsyncedUsers()
            val functions = FirebaseManager.functions
            for (user in unsyncedUsers) {
                if (user.uid.isNotEmpty()) {
                    try {
                        if (functions != null) {
                            val functionName = if (user.isVerified) "approveUser" else "rejectUser"
                            try {
                                functions.getHttpsCallable(functionName)
                                    .call(mapOf("userId" to user.uid))
                                    .await()
                            } catch (e: Exception) {
                                android.util.Log.e("SyncWorker", "Error re-invoking callable $functionName for ${user.uid}: ${e.message}")
                            }
                        }

                        val userMap = user.toFirestoreMap()
                        firestore.collection("users").document(user.uid).set(userMap).await()
                        appDao.updateUser(user.copy(pendingSync = false))
                    } catch (e: Exception) {
                        android.util.Log.e("SyncWorker", "Error syncing user ${user.uid}: ${e.message}", e)
                        hasFailure = true
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Error syncing users stage: ${e.message}", e)
            hasFailure = true
        }

        // 2. Sync Unsynced Listings
        try {
            val unsyncedListings = appDao.getUnsyncedListings()
            for (listing in unsyncedListings) {
                try {
                    val docRef = if (listing.firestoreId.isNotEmpty() && !listing.firestoreId.startsWith("local_")) {
                        firestore.collection("listings").document(listing.firestoreId)
                    } else {
                        firestore.collection("listings").document()
                    }
                    
                    val finalListing = listing.copy(firestoreId = docRef.id, pendingSync = false)
                    val listingMap = finalListing.toFirestoreMap()
                    
                    docRef.set(listingMap).await()
                    // Persist the newly-assigned firestoreId and cleared pendingSync to Room immediately
                    appDao.updateListing(finalListing)
                } catch (e: Exception) {
                    android.util.Log.e("SyncWorker", "Error syncing listing ${listing.id}: ${e.message}", e)
                    hasFailure = true
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Error syncing listings stage: ${e.message}", e)
            hasFailure = true
        }

        // 3. Sync Unsynced Chef Profiles
        try {
            val unsyncedChefs = appDao.getUnsyncedChefProfiles()
            for (chef in unsyncedChefs) {
                if (chef.uid.isNotEmpty()) {
                    try {
                        firestore.collection("chefProfiles").document(chef.uid).set(chef.toFirestoreMap()).await()
                        appDao.updateChefProfile(chef.copy(pendingSync = false))
                    } catch (e: Exception) {
                        android.util.Log.e("SyncWorker", "Error syncing chef profile ${chef.uid}: ${e.message}", e)
                        hasFailure = true
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Error syncing chef profiles stage: ${e.message}", e)
            hasFailure = true
        }

        // 4. Sync Unsynced Menu Items
        try {
            val unsyncedMenuItems = appDao.getUnsyncedMenuItems()
            for (item in unsyncedMenuItems) {
                try {
                    val docRef = if (item.firestoreId.isNotEmpty() && !item.firestoreId.startsWith("local_")) {
                        firestore.collection("menuItems").document(item.firestoreId)
                    } else {
                        firestore.collection("menuItems").document()
                    }
                    val finalItem = item.copy(firestoreId = docRef.id, pendingSync = false)
                    docRef.set(finalItem.toFirestoreMap()).await()
                    // Persist the newly-assigned firestoreId and cleared pendingSync to Room immediately
                    appDao.updateMenuItem(finalItem)
                } catch (e: Exception) {
                    android.util.Log.e("SyncWorker", "Error syncing menu item ${item.id}: ${e.message}", e)
                    hasFailure = true
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Error syncing menu items stage: ${e.message}", e)
            hasFailure = true
        }

        // 5. Sync Unsynced Meal Orders
        try {
            val unsyncedOrders = appDao.getUnsyncedMealOrders()
            for (order in unsyncedOrders) {
                try {
                    val docRef = if (order.firestoreId.isNotEmpty() && !order.firestoreId.startsWith("local_")) {
                        firestore.collection("mealOrders").document(order.firestoreId)
                    } else {
                        firestore.collection("mealOrders").document()
                    }
                    val finalOrder = order.copy(firestoreId = docRef.id, pendingSync = false)
                    docRef.set(finalOrder.toFirestoreMap()).await()
                    // Persist the newly-assigned firestoreId and cleared pendingSync to Room immediately
                    appDao.updateMealOrder(finalOrder)
                } catch (e: Exception) {
                    android.util.Log.e("SyncWorker", "Error syncing meal order ${order.id}: ${e.message}", e)
                    hasFailure = true
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Error syncing meal orders stage: ${e.message}", e)
            hasFailure = true
        }

        // 6. Sync Unsynced Meal Subscriptions
        try {
            val unsyncedSubs = appDao.getUnsyncedMealSubscriptions()
            for (sub in unsyncedSubs) {
                try {
                    val docRef = if (sub.firestoreId.isNotEmpty() && !sub.firestoreId.startsWith("local_")) {
                        firestore.collection("mealSubscriptions").document(sub.firestoreId)
                    } else {
                        firestore.collection("mealSubscriptions").document()
                    }
                    val finalSub = sub.copy(firestoreId = docRef.id, pendingSync = false)
                    docRef.set(finalSub.toFirestoreMap()).await()
                    // Persist the newly-assigned firestoreId and cleared pendingSync to Room immediately
                    appDao.updateMealSubscription(finalSub)
                } catch (e: Exception) {
                    android.util.Log.e("SyncWorker", "Error syncing meal subscription ${sub.id}: ${e.message}", e)
                    hasFailure = true
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SyncWorker", "Error syncing meal subscriptions stage: ${e.message}", e)
            hasFailure = true
        }

        return if (hasFailure) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
