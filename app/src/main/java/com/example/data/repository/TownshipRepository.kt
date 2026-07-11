package com.example.data.repository

import com.example.data.FirebaseManager
import com.example.data.await
import com.example.data.local.AppDao
import com.example.data.local.ListingEntity
import com.example.data.local.UserEntity
import com.example.data.local.toFirestoreMap
import com.example.data.local.withSerializedDetails
import com.example.data.toListingEntity
import com.example.data.toUserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class TownshipRepository(private val appDao: AppDao, private val context: android.content.Context) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private fun scheduleSyncJob() {
        try {
            val constraints = androidx.work.Constraints.Builder()
                .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                .build()

            val syncRequest = androidx.work.OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    androidx.work.BackoffPolicy.EXPONENTIAL,
                    androidx.work.WorkRequest.MIN_BACKOFF_MILLIS,
                    java.util.concurrent.TimeUnit.MILLISECONDS
                )
                .build()

            androidx.work.WorkManager.getInstance(context).enqueueUniqueWork(
                "ConnectKarSyncWork",
                androidx.work.ExistingWorkPolicy.REPLACE,
                syncRequest
            )
        } catch (e: Exception) {
            android.util.Log.e("SyncJob", "Failed to schedule sync job: ${e.message}")
        }
    }

    val syncWorkInfo: Flow<List<androidx.work.WorkInfo>> = 
        androidx.work.WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow("ConnectKarSyncWork")

    fun triggerSync() {
        scheduleSyncJob()
    }

    val allUsers: Flow<List<UserEntity>> = appDao.getAllUsers()
    val currentUserFlow: Flow<UserEntity?> = appDao.getCurrentUserFlow()
    val allListings: Flow<List<ListingEntity>> = appDao.getAllListings()

    init {
        startFirestoreSync()
    }

    private fun startFirestoreSync() {
        val firestore = FirebaseManager.firestore ?: return
        
        // Listen for users in Firestore and keep Room updated
        scope.launch {
            try {
                firestore.collection("users").limit(100).addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("Sync", "Users snapshot listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val user = doc.toUserEntity()
                                if (user != null) {
                                    val existing = appDao.getUserByUidDirect(user.uid)
                                    if (existing != null) {
                                        // Conflict Resolution: Only update if no local unsynced edits are pending
                                        if (!existing.pendingSync) {
                                            // Keep current flag of local user intact
                                            appDao.updateUser(user.copy(id = existing.id, isCurrent = existing.isCurrent))
                                        }
                                    } else {
                                        appDao.insertUser(user)
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Sync", "Users sync failed to start", e)
            }
        }

        // Listen for listings in Firestore and keep Room updated
        scope.launch {
            try {
                firestore.collection("listings")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(100)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            android.util.Log.e("Sync", "Listings snapshot listener error", error)
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            scope.launch {
                                for (doc in snapshot.documents) {
                                    val listing = doc.toListingEntity()
                                    if (listing != null) {
                                        val existing = appDao.getListingByFirestoreIdDirect(listing.firestoreId)
                                        if (existing != null) {
                                            // Conflict Resolution: Avoid clobbering pending local edits unless remote is newer
                                            if (existing.pendingSync) {
                                                if (listing.timestamp > existing.timestamp) {
                                                    appDao.updateListing(listing.copy(id = existing.id))
                                                }
                                            } else {
                                                appDao.updateListing(listing.copy(id = existing.id))
                                            }
                                        } else {
                                            appDao.insertListing(listing)
                                        }
                                    }
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.e("Sync", "Listings sync failed to start", e)
            }
        }
    }

    fun getListingsByType(type: String): Flow<List<ListingEntity>> = appDao.getListingsByType(type)
    
    fun getListingsByTypeAndSociety(type: String, society: String): Flow<List<ListingEntity>> {
        return if (society.isEmpty() || society == "All Societies") {
            appDao.getListingsByType(type)
        } else {
            appDao.getListingsByTypeAndSociety(type, society)
        }
    }

    suspend fun getCurrentUser(): UserEntity? = appDao.getCurrentUserDirect()

    // Modified registerUser to write to Firestore
    suspend fun registerUser(
        fullName: String,
        phoneNumber: String,
        society: String,
        blockTower: String,
        flatNumber: String,
        avatarIndex: Int,
        floor: String = "",
        residentType: String = "OWNER",
        moveInDate: String = "",
        proofDocumentUri: String = ""
    ): UserEntity {
        // Clear previous current users
        appDao.clearCurrentUserFlag()
        
        val uid = FirebaseManager.auth?.currentUser?.uid ?: "user_${System.currentTimeMillis()}"
        
        val newUser = UserEntity(
            uid = uid,
            fullName = fullName,
            phoneNumber = phoneNumber,
            society = society,
            blockTower = blockTower,
            flatNumber = flatNumber,
            avatarIndex = avatarIndex,
            isVerified = false,
            isPending = true,
            isCurrent = true,
            role = "RESIDENT",
            pendingSync = false,
            floor = floor,
            residentType = residentType,
            moveInDate = moveInDate,
            proofDocumentUri = proofDocumentUri
        )
        
        val insertedId = appDao.insertUser(newUser)
        var finalUser = newUser.copy(id = insertedId.toInt())
        
        // Write to Firestore if available
        val firestore = FirebaseManager.firestore
        if (firestore != null && uid.isNotEmpty()) {
            val userMap = finalUser.toFirestoreMap()
            try {
                firestore.collection("users").document(uid).set(userMap).await()
            } catch (e: Exception) {
                android.util.Log.e("Firestore", "Error saving user: ${e.message}")
                finalUser = finalUser.copy(pendingSync = true)
                appDao.updateUser(finalUser)
                scheduleSyncJob()
            }
        } else {
            finalUser = finalUser.copy(pendingSync = true)
            appDao.updateUser(finalUser)
            scheduleSyncJob()
        }
        
        return finalUser
    }

    suspend fun loginAsUser(user: UserEntity) {
        appDao.clearCurrentUserFlag()
        appDao.updateUser(user.copy(isCurrent = true))
        
        // Also update firestore user role / status if available
        val firestore = FirebaseManager.firestore
        if (firestore != null && user.uid.isNotEmpty()) {
            try {
                firestore.collection("users").document(user.uid).update("isCurrent", true)
            } catch (e: Exception) {
                android.util.Log.e("Firestore", "Error updating current status: ${e.message}")
            }
        }
    }

    suspend fun logout() {
        // Sign out from FirebaseAuth
        FirebaseManager.auth?.signOut()
        appDao.clearCurrentUserFlag()
    }

    suspend fun updateVerificationStatus(userId: Int, isVerified: Boolean) {
        val user = appDao.getUserById(userId)
        if (user != null) {
            val updatedUser = user.copy(
                isVerified = isVerified,
                isPending = false
            )
            appDao.updateUser(updatedUser)
            
            // Call Cloud Function callable
            val functions = FirebaseManager.functions
            if (functions != null && user.uid.isNotEmpty()) {
                val functionName = if (isVerified) "approveUser" else "rejectUser"
                try {
                    functions.getHttpsCallable(functionName)
                        .call(mapOf("userId" to user.uid))
                        .await()
                } catch (e: Exception) {
                    android.util.Log.e("Functions", "Error calling $functionName: ${e.message}")
                    // In debug mode, if the Cloud Function is not deployed, fallback to direct Firestore write for convenience
                    if (com.example.BuildConfig.DEBUG) {
                        val firestore = FirebaseManager.firestore
                        if (firestore != null) {
                            try {
                                firestore.collection("users").document(user.uid).update(
                                    "isVerified", isVerified,
                                    "isPending", false
                                ).await()
                            } catch (fe: Exception) {
                                android.util.Log.e("Firestore", "Debug direct fallback failed: ${fe.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modified insertListing to write to Firestore first and then cache locally
    suspend fun insertListing(listing: ListingEntity) {
        val typedListing = listing.withSerializedDetails()
        val firestore = FirebaseManager.firestore
        if (firestore != null) {
            val docRef = firestore.collection("listings").document()
            val finalListing = typedListing.copy(firestoreId = docRef.id, pendingSync = false)
            val listingMap = finalListing.toFirestoreMap()
            try {
                docRef.set(listingMap).await()
                appDao.insertListing(finalListing)
            } catch (e: Exception) {
                android.util.Log.e("Firestore", "Error creating listing: ${e.message}")
                val fallbackListing = typedListing.copy(firestoreId = docRef.id, pendingSync = true)
                appDao.insertListing(fallbackListing) // fallback to local with pendingSync
                scheduleSyncJob()
            }
        } else {
            val fallbackListing = typedListing.copy(firestoreId = "local_${System.currentTimeMillis()}", pendingSync = true)
            appDao.insertListing(fallbackListing)
            scheduleSyncJob()
        }
    }

    suspend fun deleteListing(listingId: Int) {
        val listing = appDao.getListingById(listingId)
        if (listing != null) {
            appDao.deleteListingById(listingId)
            
            val firestore = FirebaseManager.firestore
            if (firestore != null && listing.firestoreId.isNotEmpty()) {
                try {
                    firestore.collection("listings").document(listing.firestoreId).delete().await()
                } catch (e: Exception) {
                    android.util.Log.e("Firestore", "Error deleting listing: ${e.message}")
                }
            }
        }
    }

    suspend fun toggleLikeListing(listingId: Int) {
        val listing = appDao.getListingById(listingId)
        if (listing != null) {
            val newLiked = !listing.isLikedByMe
            val newCount = if (newLiked) listing.likesCount + 1 else maxOf(0, listing.likesCount - 1)
            val updated = listing.copy(isLikedByMe = newLiked, likesCount = newCount)
            appDao.updateListing(updated)
            
            val firestore = FirebaseManager.firestore
            if (firestore != null && listing.firestoreId.isNotEmpty()) {
                try {
                    firestore.collection("listings").document(listing.firestoreId).update(
                        "isLikedByMe", newLiked,
                        "likesCount", newCount
                    ).await()
                } catch (e: Exception) {
                    android.util.Log.e("Firestore", "Error toggling like: ${e.message}")
                }
            }
        }
    }

    suspend fun toggleBookmarkListing(listingId: Int) {
        val listing = appDao.getListingById(listingId)
        if (listing != null) {
            val updated = listing.copy(isBookmarked = !listing.isBookmarked)
            appDao.updateListing(updated)
            
            val firestore = FirebaseManager.firestore
            if (firestore != null && listing.firestoreId.isNotEmpty()) {
                try {
                    firestore.collection("listings").document(listing.firestoreId).update(
                        "isBookmarked", updated.isBookmarked
                    ).await()
                } catch (e: Exception) {
                    android.util.Log.e("Firestore", "Error toggling bookmark: ${e.message}")
                }
            }
        }
    }

    suspend fun seedMockData() {
        val existingUsers = allUsers.firstOrNull()
        if (existingUsers.isNullOrEmpty()) {
            val admin = UserEntity(
                uid = "mock_admin_uid_123",
                fullName = "Ananya Sen",
                phoneNumber = "9000012345",
                society = "Sylvan County",
                blockTower = "Admin Tower",
                flatNumber = "101",
                avatarIndex = 4,
                isVerified = true,
                isPending = false,
                isCurrent = false,
                role = "ADMIN"
            )
            appDao.insertUser(admin)

            val resident1 = UserEntity(
                uid = "mock_res_uid_rohan",
                fullName = "Rohan Gupta",
                phoneNumber = "9876543210",
                society = "Sylvan County",
                blockTower = "Block C",
                flatNumber = "C-903",
                avatarIndex = 1,
                isVerified = true,
                isPending = false,
                isCurrent = false,
                role = "RESIDENT"
            )
            appDao.insertUser(resident1)

            val resident2 = UserEntity(
                uid = "mock_res_uid_sneha",
                fullName = "Sneha Reddy",
                phoneNumber = "9812345678",
                society = "Nova Apartments",
                blockTower = "Tower 2",
                flatNumber = "D-504",
                avatarIndex = 2,
                isVerified = true,
                isPending = false,
                isCurrent = false,
                role = "RESIDENT"
            )
            appDao.insertUser(resident2)
        }

        val existingListings = allListings.firstOrNull()
        if (existingListings.isNullOrEmpty()) {
            val defaultListings = listOf(
                ListingEntity(
                    type = "MARKETPLACE",
                    title = "Wooden Study Table (Teak)",
                    description = "Teakwood study table with 3 spacious drawers and elegant keyboard tray. Only 1.5 years old, immaculate condition.",
                    price = 3200.0,
                    contact = "9876543210",
                    society = "Sylvan County",
                    authorName = "Rohan Gupta",
                    authorFlat = "C-903",
                    authorPhone = "9876543210",
                    category = "Furniture",
                    extra1 = "Slightly Used",
                    extra2 = "Teakwood"
                ),
                ListingEntity(
                    type = "MARKETPLACE",
                    title = "Kids Decathlon Rockrider Bicycle",
                    description = "Red and black junior mountain bike. Ideal for ages 6-10 years. Double brakes, front suspension, barely ridden on campus pavements.",
                    price = 2200.0,
                    contact = "9812345678",
                    society = "Nova Apartments",
                    authorName = "Sneha Reddy",
                    authorFlat = "D-504",
                    authorPhone = "9812345678",
                    category = "Sports & Fitness",
                    extra1 = "Excellent Condition",
                    extra2 = "6-10 Years"
                ),
                ListingEntity(
                    type = "SERVICE",
                    title = "Pro Electrician & Geyser Care",
                    description = "Available for urgent electrical repairs, smart home installations, fan/geyser replacement, and fuse fixes inside Sylvan community.",
                    price = 150.0,
                    contact = "9123456780",
                    society = "Sylvan County",
                    authorName = "Ramesh Kumar",
                    authorFlat = "Services Bay 1",
                    authorPhone = "9123456780",
                    category = "Electrical",
                    extra4 = "4.9"
                ),
                ListingEntity(
                    type = "FEED",
                    title = "Lost Car Keys near Central Playground",
                    description = "Dropped a black leather key fob for a Hyundai Creta near the children's sandbox around 6:30 PM today. If found, please return it! Thanks.",
                    price = 0.0,
                    contact = "9988776655",
                    society = "Sylvan County",
                    authorName = "Aman Preet",
                    authorFlat = "B-201",
                    authorPhone = "9988776655",
                    category = "General Alert"
                )
            )

            for (listing in defaultListings) {
                val typedListing = listing.withSerializedDetails()
                val firestore = FirebaseManager.firestore
                if (firestore != null) {
                    val docRef = firestore.collection("listings").document()
                    val seeded = typedListing.copy(firestoreId = docRef.id)
                    val listingMap = seeded.toFirestoreMap()
                    try {
                        docRef.set(listingMap)
                    } catch (e: Exception) {}
                    appDao.insertListing(seeded)
                } else {
                    appDao.insertListing(typedListing)
                }
            }
        }
    }
}
