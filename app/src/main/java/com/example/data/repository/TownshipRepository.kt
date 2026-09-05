package com.example.data.repository

import com.example.data.FirebaseManager
import com.example.data.await
import com.example.data.local.AppDao
import com.example.data.local.ListingEntity
import com.example.data.local.UserEntity
import com.example.data.local.ChefProfileEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.MealOrderEntity
import com.example.data.local.MealSubscriptionEntity
import com.example.data.local.toFirestoreMap
import com.example.data.local.withSerializedDetails
import com.example.data.toListingEntity
import com.example.data.toUserEntity
import com.example.data.toChefProfileEntity
import com.example.data.toMenuItemEntity
import com.example.data.toMealOrderEntity
import com.example.data.toMealSubscriptionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class TownshipRepository(
    private val appDao: AppDao,
    private val context: android.content.Context,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore? = FirebaseManager.firestore
) : java.io.Closeable {

    private val repositoryJob = kotlinx.coroutines.SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + repositoryJob)

    private var usersListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var listingsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var chefProfilesListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var menuItemsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var mealOrdersListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var mealSubscriptionsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // For pagination
    private var lastListingsVisible: com.google.firebase.firestore.DocumentSnapshot? = null
    private var isListingsLoading = false
    private var hasMoreListings = true

    override fun close() {
        repositoryJob.cancel()
        removeListeners()
    }

    fun removeListeners() {
        usersListenerRegistration?.remove()
        usersListenerRegistration = null
        listingsListenerRegistration?.remove()
        listingsListenerRegistration = null
        chefProfilesListenerRegistration?.remove()
        chefProfilesListenerRegistration = null
        menuItemsListenerRegistration?.remove()
        menuItemsListenerRegistration = null
        mealOrdersListenerRegistration?.remove()
        mealOrdersListenerRegistration = null
        mealSubscriptionsListenerRegistration?.remove()
        mealSubscriptionsListenerRegistration = null
    }

    private fun scheduleSyncJob() {
        SyncWorker.enqueueSync(context)
    }

    val syncWorkInfo: Flow<List<androidx.work.WorkInfo>> by lazy {
        try {
            androidx.work.WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow(SyncWorker.UNIQUE_WORK_NAME)
        } catch (e: Exception) {
            android.util.Log.e("TownshipRepository", "WorkManager unavailable: ${e.message}")
            kotlinx.coroutines.flow.emptyFlow()
        }
    }

    fun triggerSync() {
        scheduleSyncJob()
    }

    suspend fun manualSync(): Result<Unit> {
        val fs = firestore
        return try {
            if (fs != null) {
                // 1. Push pending local changes
                val unsyncedUsers = appDao.getUnsyncedUsers()
                val fns = FirebaseManager.functions
                for (user in unsyncedUsers) {
                    if (user.uid.isNotEmpty()) {
                        if (fns != null) {
                            val functionName = if (user.isVerified) "approveUser" else "rejectUser"
                            try {
                                fns.getHttpsCallable(functionName)
                                    .call(mapOf("userId" to user.uid))
                                    .await()
                            } catch (e: Exception) {
                                android.util.Log.e("ManualSync", "Error re-invoking callable $functionName for ${user.uid}: ${e.message}")
                            }
                        }
                        fs.collection("users").document(user.uid).set(user.toFirestoreMap()).await()
                        appDao.updateUser(user.copy(pendingSync = false))
                    }
                }

                val unsyncedListings = appDao.getUnsyncedListings()
                for (listing in unsyncedListings) {
                    val docRef = if (listing.firestoreId.isNotEmpty() && !listing.firestoreId.startsWith("local_")) {
                        fs.collection("listings").document(listing.firestoreId)
                    } else {
                        fs.collection("listings").document()
                    }
                    val finalListing = listing.copy(firestoreId = docRef.id, pendingSync = false)
                    docRef.set(finalListing.toFirestoreMap()).await()
                    appDao.updateListing(finalListing)
                }

                val unsyncedChefs = appDao.getUnsyncedChefProfiles()
                for (chef in unsyncedChefs) {
                    if (chef.uid.isNotEmpty()) {
                        fs.collection("chefProfiles").document(chef.uid).set(chef.toFirestoreMap()).await()
                        appDao.updateChefProfile(chef.copy(pendingSync = false))
                    }
                }

                val unsyncedMenuItems = appDao.getUnsyncedMenuItems()
                for (item in unsyncedMenuItems) {
                    val docRef = if (item.firestoreId.isNotEmpty() && !item.firestoreId.startsWith("local_")) {
                        fs.collection("menuItems").document(item.firestoreId)
                    } else {
                        fs.collection("menuItems").document()
                    }
                    val finalItem = item.copy(firestoreId = docRef.id, pendingSync = false)
                    docRef.set(finalItem.toFirestoreMap()).await()
                    appDao.updateMenuItem(finalItem)
                }

                val unsyncedOrders = appDao.getUnsyncedMealOrders()
                for (order in unsyncedOrders) {
                    val docRef = if (order.firestoreId.isNotEmpty() && !order.firestoreId.startsWith("local_")) {
                        fs.collection("mealOrders").document(order.firestoreId)
                    } else {
                        fs.collection("mealOrders").document()
                    }
                    val finalOrder = order.copy(firestoreId = docRef.id, pendingSync = false)
                    docRef.set(finalOrder.toFirestoreMap()).await()
                    appDao.updateMealOrder(finalOrder)
                }

                val unsyncedSubs = appDao.getUnsyncedMealSubscriptions()
                for (sub in unsyncedSubs) {
                    val docRef = if (sub.firestoreId.isNotEmpty() && !sub.firestoreId.startsWith("local_")) {
                        fs.collection("mealSubscriptions").document(sub.firestoreId)
                    } else {
                        fs.collection("mealSubscriptions").document()
                    }
                    val finalSub = sub.copy(firestoreId = docRef.id, pendingSync = false)
                    docRef.set(finalSub.toFirestoreMap()).await()
                    appDao.updateMealSubscription(finalSub)
                }

                // 2. Fetch fresh remote data for current society
                val currentUser = getCurrentUser()
                val society = currentUser?.society ?: ""
                if (society.isNotEmpty()) {
                    val listingsSnap = fs.collection("listings")
                        .whereEqualTo("society", society)
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .limit(20)
                        .get().await()

                    for (doc in listingsSnap.documents) {
                        val listing = doc.toListingEntity()
                        if (listing != null) {
                            val existing = appDao.getListingByFirestoreIdDirect(listing.firestoreId)
                            if (existing != null) {
                                if (!existing.pendingSync || listing.timestamp > existing.timestamp) {
                                    appDao.updateListing(listing.copy(id = existing.id))
                                }
                            } else {
                                appDao.insertListing(listing)
                            }
                        }
                    }

                    val menuSnap = fs.collection("menuItems")
                        .whereEqualTo("society", society)
                        .get().await()
                    for (doc in menuSnap.documents) {
                        val item = doc.toMenuItemEntity()
                        if (item != null) {
                            val existing = appDao.getMenuItemByFirestoreIdDirect(item.firestoreId)
                            if (existing != null) {
                                if (!existing.pendingSync || item.timestamp > existing.timestamp) {
                                    appDao.updateMenuItem(item.copy(id = existing.id))
                                }
                            } else {
                                appDao.insertMenuItem(item)
                            }
                        }
                    }
                }
            } else {
                scheduleSyncJob()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("ManualSync", "Manual sync error: ${e.message}", e)
            scheduleSyncJob()
            Result.failure(e)
        }
    }

    val allUsers: Flow<List<UserEntity>> = appDao.getAllUsers()
    val currentUserFlow: Flow<UserEntity?> = appDao.getCurrentUserFlow()
    val allListings: Flow<List<ListingEntity>> = appDao.getAllListings()

    init {
        // Start reactive sync based on current user's society
        scope.launch {
            try {
                currentUserFlow
                    .map { it?.society ?: "" }
                    .distinctUntilChanged()
                    .collect { society ->
                        if (society.isNotEmpty()) {
                            restartFirestoreSync(society)
                        } else {
                            removeListeners()
                        }
                    }
            } catch (e: Exception) {
                android.util.Log.w("Sync", "Sync collection closed/cancelled: ${e.message}")
            }
        }
    }

    private fun restartFirestoreSync(society: String) {
        removeListeners()
        val fs = firestore ?: return

        // Reset pagination for this society
        lastListingsVisible = null
        isListingsLoading = false
        hasMoreListings = true

        // Listen for users in the same society in Firestore and keep Room updated
        try {
            usersListenerRegistration = fs.collection("users")
                .whereEqualTo("society", society)
                .limit(50)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("Sync", "Users snapshot listener error", error)
                        usersListenerRegistration = null
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val user = doc.toUserEntity()
                                if (user != null) {
                                    val existing = appDao.getUserByUidDirect(user.uid)
                                    if (existing != null) {
                                        // Conflict Resolution: Only update if no local unsynced edits are pending or remote timestamp is newer
                                        if (!existing.pendingSync || user.timestamp > existing.timestamp) {
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

        // Listen for listings in the same society in Firestore and keep Room updated
        try {
            listingsListenerRegistration = fs.collection("listings")
                .whereEqualTo("society", society)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("Sync", "Listings snapshot listener error", error)
                        listingsListenerRegistration = null
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            if (snapshot.documents.isNotEmpty()) {
                                lastListingsVisible = snapshot.documents.last()
                            }
                            for (doc in snapshot.documents) {
                                val listing = doc.toListingEntity()
                                if (listing != null) {
                                    val existing = appDao.getListingByFirestoreIdDirect(listing.firestoreId)
                                    if (existing != null) {
                                        // Conflict Resolution: Avoid clobbering pending local edits unless remote is newer
                                        if (!existing.pendingSync || listing.timestamp > existing.timestamp) {
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

        // Listen for chefProfiles in Firestore
        try {
            chefProfilesListenerRegistration = fs.collection("chefProfiles")
                .whereEqualTo("society", society)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("Sync", "ChefProfiles snapshot listener error", error)
                        chefProfilesListenerRegistration = null
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val chef = doc.toChefProfileEntity()
                                if (chef != null) {
                                    val existing = appDao.getChefProfileByUidDirect(chef.uid)
                                    if (existing != null) {
                                        if (!existing.pendingSync || chef.timestamp > existing.timestamp) {
                                            appDao.updateChefProfile(chef)
                                        }
                                    } else {
                                        appDao.insertChefProfile(chef)
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("Sync", "ChefProfiles sync failed to start", e)
        }

        // Listen for menuItems in Firestore
        try {
            menuItemsListenerRegistration = fs.collection("menuItems")
                .whereEqualTo("society", society)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("Sync", "MenuItems snapshot listener error", error)
                        menuItemsListenerRegistration = null
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val item = doc.toMenuItemEntity()
                                if (item != null) {
                                    val existing = appDao.getMenuItemByFirestoreIdDirect(item.firestoreId)
                                    if (existing != null) {
                                        if (!existing.pendingSync || item.timestamp > existing.timestamp) {
                                            appDao.updateMenuItem(item.copy(id = existing.id))
                                        }
                                    } else {
                                        appDao.insertMenuItem(item)
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("Sync", "MenuItems sync failed to start", e)
        }

        // Listen for mealOrders in Firestore
        try {
            mealOrdersListenerRegistration = fs.collection("mealOrders")
                .whereEqualTo("society", society)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("Sync", "MealOrders snapshot listener error", error)
                        mealOrdersListenerRegistration = null
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val order = doc.toMealOrderEntity()
                                if (order != null) {
                                    val existing = appDao.getOrderByFirestoreIdDirect(order.firestoreId)
                                    if (existing != null) {
                                        if (!existing.pendingSync || order.timestamp > existing.timestamp) {
                                            appDao.updateMealOrder(order.copy(id = existing.id))
                                        }
                                    } else {
                                        appDao.insertMealOrder(order)
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("Sync", "MealOrders sync failed to start", e)
        }

        // Listen for mealSubscriptions in Firestore
        try {
            mealSubscriptionsListenerRegistration = fs.collection("mealSubscriptions")
                .whereEqualTo("society", society)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        android.util.Log.e("Sync", "MealSubscriptions snapshot listener error", error)
                        mealSubscriptionsListenerRegistration = null
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            for (doc in snapshot.documents) {
                                val sub = doc.toMealSubscriptionEntity()
                                if (sub != null) {
                                    val existing = appDao.getSubscriptionByFirestoreIdDirect(sub.firestoreId)
                                    if (existing != null) {
                                        if (!existing.pendingSync || sub.timestamp > existing.timestamp) {
                                            appDao.updateMealSubscription(sub.copy(id = existing.id))
                                        }
                                    } else {
                                        appDao.insertMealSubscription(sub)
                                    }
                                }
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            android.util.Log.e("Sync", "MealSubscriptions sync failed to start", e)
        }
    }

    suspend fun loadNextPageListings(society: String, type: String) {
        if (isListingsLoading || !hasMoreListings) return
        val fs = firestore ?: return
        isListingsLoading = true
        try {
            var query = fs.collection("listings")
                .whereEqualTo("society", society)
                .whereEqualTo("type", type)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)
            
            val last = lastListingsVisible
            if (last != null) {
                query = query.startAfter(last)
            }
            
            val snapshot = query.get().await()
            if (!snapshot.isEmpty) {
                lastListingsVisible = snapshot.documents.lastOrNull()
                if (snapshot.size() < 20) {
                    hasMoreListings = false
                }
                val entities = snapshot.documents.mapNotNull { it.toListingEntity() }
                for (entity in entities) {
                    val existing = appDao.getListingByFirestoreIdDirect(entity.firestoreId)
                    if (existing != null) {
                        if (!existing.pendingSync || entity.timestamp > existing.timestamp) {
                            appDao.updateListing(entity.copy(id = existing.id))
                        }
                    } else {
                        appDao.insertListing(entity)
                    }
                }
            } else {
                hasMoreListings = false
            }
        } catch (e: Exception) {
            android.util.Log.e("Sync", "Error paginating listings", e)
        } finally {
            isListingsLoading = false
        }
    }

    fun getListingsByType(type: String): Flow<List<ListingEntity>> = appDao.getListingsByType(type)
    
    fun getListingsByAuthor(uid: String): Flow<List<ListingEntity>> = appDao.getListingsByAuthor(uid)

    fun getBookmarkedListingsByType(type: String): Flow<List<ListingEntity>> = appDao.getBookmarkedListingsByType(type)
    
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
                firestore.collection("users").document(user.uid).update(
                    "isCurrent", true,
                    "timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "serverTimestamp", com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
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
                isPending = false,
                pendingSync = false
            )
            appDao.updateUser(updatedUser)
            
            // Call Cloud Function callable (or directly update Firestore)
            val functions = FirebaseManager.functions
            val firestore = FirebaseManager.firestore
            var functionSucceeded = false
            
            if (functions != null && user.uid.isNotEmpty()) {
                val functionName = if (isVerified) "approveUser" else "rejectUser"
                try {
                    functions.getHttpsCallable(functionName)
                        .call(mapOf("userId" to user.uid))
                        .await()
                    functionSucceeded = true
                } catch (e: Exception) {
                    android.util.Log.e("Functions", "Error calling $functionName: ${e.message}")
                }
            }
            
            if (!functionSucceeded) {
                // If Cloud Function threw or functions unavailable, attempt direct Firestore write or mark pendingSync = true
                if (firestore != null && user.uid.isNotEmpty()) {
                    try {
                        firestore.collection("users").document(user.uid).update(
                            mapOf(
                                "isVerified" to isVerified,
                                "isPending" to false,
                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                        ).await()
                        functionSucceeded = true
                    } catch (e: Exception) {
                        android.util.Log.e("Firestore", "Error direct-updating user verification: ${e.message}")
                    }
                }
            }

            if (!functionSucceeded) {
                // Mark user row as pendingSync = true so SyncWorker will retry, and schedule background sync
                appDao.updateUser(updatedUser.copy(pendingSync = true))
                scheduleSyncJob()
            }
        }
    }

    suspend fun getDraftListing(type: String): ListingEntity? {
        return appDao.getDraftListingByTypeDirect(type)
    }

    suspend fun saveDraftListing(listing: ListingEntity): Long {
        val typedListing = listing.copy(isDraft = true).withSerializedDetails()
        return appDao.insertListing(typedListing)
    }

    suspend fun deleteDraft(id: Int) {
        appDao.deleteListingById(id)
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
            val updated = listing.copy(
                isLikedByMe = newLiked,
                likesCount = newCount,
                timestamp = System.currentTimeMillis()
            )
            appDao.updateListing(updated)
            
            val firestore = FirebaseManager.firestore
            if (firestore != null && listing.firestoreId.isNotEmpty()) {
                try {
                    firestore.collection("listings").document(listing.firestoreId).update(
                        "isLikedByMe", newLiked,
                        "likesCount", newCount,
                        "timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "serverTimestamp", com.google.firebase.firestore.FieldValue.serverTimestamp()
                    ).await()
                } catch (e: Exception) {
                    android.util.Log.e("Firestore", "Error toggling like: ${e.message}")
                    appDao.updateListing(updated.copy(pendingSync = true))
                    scheduleSyncJob()
                }
            } else {
                appDao.updateListing(updated.copy(pendingSync = true))
                scheduleSyncJob()
            }
        }
    }

    suspend fun toggleBookmarkListing(listingId: Int) {
        val listing = appDao.getListingById(listingId)
        if (listing != null) {
            val updated = listing.copy(
                isBookmarked = !listing.isBookmarked,
                timestamp = System.currentTimeMillis()
            )
            appDao.updateListing(updated)
            
            val firestore = FirebaseManager.firestore
            if (firestore != null && listing.firestoreId.isNotEmpty()) {
                try {
                    firestore.collection("listings").document(listing.firestoreId).update(
                        "isBookmarked", updated.isBookmarked,
                        "timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "serverTimestamp", com.google.firebase.firestore.FieldValue.serverTimestamp()
                    ).await()
                } catch (e: Exception) {
                    android.util.Log.e("Firestore", "Error toggling bookmark: ${e.message}")
                    appDao.updateListing(updated.copy(pendingSync = true))
                    scheduleSyncJob()
                }
            } else {
                appDao.updateListing(updated.copy(pendingSync = true))
                scheduleSyncJob()
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
                ),
                ListingEntity(
                    type = "PROPERTY",
                    title = "2 BHK Semi-Furnished Apartment",
                    description = "Modular kitchen, wardrobes in both bedrooms, and excellent ventilation.",
                    price = 28000.0,
                    contact = "9876543210",
                    society = "",
                    authorName = "Ananya Sen",
                    authorFlat = "Wing A, Flat 304",
                    authorPhone = "9876543210",
                    category = "2 BHK Rent",
                    extra1 = "https://lh3.googleusercontent.com/aida-public/AB6AXuA1ilwu0-nL4Uf4RDnlpLjtgUgVcugQkNHj9n-5km498WAcH_Yp290Dxq7oDHFCSUpMJgfx5AsoC_DbRl59YgzgrghIq1GC_BhE8rekPsJSzLROBEnYSl5EM64MfXqnJn7d2ycWMMkCG-v9aptZFlP6Ad3gRbnIGZ1PbEmDv6XgkjtrYtfS7JHTD7Ubmi5cWHX1nsSccrkiZjStXigCV5NM07oLlrsJAMC0zu6YBKaj7YLurQ1XhdDx",
                    extra2 = "2 BHK",
                    extra3 = "AVAILABLE",
                    extra4 = "VERIFIED_OWNER"
                ),
                ListingEntity(
                    type = "PROPERTY",
                    title = "1 BHK Fully-Furnished Studio",
                    description = "Cosy, elegant fully-furnished studio apartment, perfect for individuals or couples. Modern appliances and sleek decor.",
                    price = 18500.0,
                    contact = "9876543210",
                    society = "",
                    authorName = "Ramesh Kumar",
                    authorFlat = "Wing B, Flat 102",
                    authorPhone = "9876543210",
                    category = "1 BHK Rent",
                    extra1 = "https://lh3.googleusercontent.com/aida-public/AB6AXuCSUHUKTUPaoc_5P-7jwaWqqa9Fj2Iy4HBoVfnObdWF0TCV8VKKCTs3QiYn1K6uCS8TCkSTsVb5E8V0FUOkflhJr4Ta65nayOSsu0MN457pZnN1sodqGAtR64wqLwNIgUz61JWqUTjYnURFpL2_tP63kb37s6F_tbjY46leBr-RgQT5dXEagELErgSr3mloYPlBzKUJ0dh2s-8s0TISlqczSEr9D1O2TdExNooiBwMQJ2NtKM3UWaz6",
                    extra2 = "1 BHK",
                    extra3 = "AVAILABLE",
                    extra4 = "VERIFIED_OWNER"
                ),
                ListingEntity(
                    type = "PROPERTY",
                    title = "3 BHK Luxury Penthouse",
                    description = "High-end penthouse featuring premium marble flooring, custom lighting, spacious private terrace with fireplace, and stunning cityscape views.",
                    price = 65000.0,
                    contact = "9123456780",
                    society = "",
                    authorName = "Sneha Reddy",
                    authorFlat = "Tower C, Penthouse 24",
                    authorPhone = "9123456780",
                    category = "3 BHK Rent",
                    extra1 = "https://lh3.googleusercontent.com/aida-public/AB6AXuC4vOoaVsOZqmaG_D_HK1YcFcmqGhDYNA8hNtVFRNWRH7vHqEySKkMI58oVmRVe2Oq8T0IijJkRwIyt-7Ri9WCrtscd4PH8VHMAG5Yx1yxl8rmGVAEASkuTyMFeolnAePXaFEe1pBmulVnymQFovQASdvPfcjyKE2UkoULrcgfxXJcIlMVT78CvWrgAx-FFHVkO27HrMfv16cSdNNfJDRFtGH21yHMgn3fZYI6S9-b0l45FVp8Wx3Wt",
                    extra2 = "3 BHK",
                    extra3 = "AVAILABLE",
                    extra4 = "SOCIETY_APPROVED"
                ),
                ListingEntity(
                    type = "PROPERTY",
                    title = "2 BHK Shared Living Space",
                    description = "Comfortable co-living setup with shared common areas, private bedrooms, and high-speed internet. Ideal for students and young professionals.",
                    price = 14000.0,
                    contact = "9988776655",
                    society = "",
                    authorName = "Rohan Gupta",
                    authorFlat = "Wing D, Flat 505",
                    authorPhone = "9988776655",
                    category = "PG / Shared Accommodation",
                    extra1 = "https://lh3.googleusercontent.com/aida-public/AB6AXuDxmY7e1rbYWoQHTX5ktiTWHoK_QhpiPw_eYId8uNdB0m1kKNPxHVXcui4Fhan3Nad-rw3nhLGgQeSzXGKAWGgIqsh4OhCmafwwaHBUOa5JOf4STa88rSUu99oqjqq2Nd2n-0hLkwoUVgCeILdKoS3tRPAWb7dWNzwKnABGiNg0jUczKQhY0n7eBpsElnclyHGC3R9_CEROej389nrf5V-hsRntEDpMJufhFkczaaosABQoi0UyKy6i",
                    extra2 = "2 BHK",
                    extra3 = "AVAILABLE",
                    extra4 = "NONE"
                ),
                ListingEntity(
                    type = "MEAL",
                    title = "Spicy Paneer Salad Bowl",
                    description = "Fresh paneer cubes with organic greens, bell peppers, tomatoes, and spicy mint-cilantro vinaigrette. High-protein, low-calorie.",
                    price = 249.0,
                    contact = "9876543210",
                    society = "Sylvan County",
                    authorName = "Priya Sharma",
                    authorFlat = "Wing A, Flat 304",
                    authorPhone = "9876543210",
                    category = "Lunch Veg",
                    extra1 = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                    extra2 = "VEG",
                    extra3 = "Within 45 mins",
                    extra4 = "HOME_CHEF"
                ),
                ListingEntity(
                    type = "MEAL",
                    title = "Nawabi Chicken Biryani",
                    description = "Fragrant basmati rice slow-cooked with tender marinated chicken and exotic spices, served with cooling raita.",
                    price = 380.0,
                    contact = "9812345678",
                    society = "Sylvan County",
                    authorName = "Vikram Malhotra",
                    authorFlat = "Wing C, Flat 902",
                    authorPhone = "9812345678",
                    category = "Lunch Non-Veg",
                    extra1 = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500",
                    extra2 = "NON-VEG",
                    extra3 = "By 1:30 PM",
                    extra4 = "HOME_CHEF"
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
                        docRef.set(listingMap).await()
                    } catch (e: Exception) {
                        android.util.Log.e("SeedData", "Error seeding listing ${seeded.title} to Firestore: ${e.message}")
                    }
                    appDao.insertListing(seeded)
                } else {
                    appDao.insertListing(typedListing)
                }
            }

            // Seed Mock Chefs & Menu Items
            val mockChefs = listOf(
                ChefProfileEntity(
                    uid = "chef_priya",
                    isChef = true,
                    chefStory = "Home cooking enthusiast specialized in authentic Satvik and North Indian thalis. Cooking with love and cold-pressed organic oils.",
                    mealsServedCount = 142,
                    regularsCount = 28,
                    ratingAvg = 4.9,
                    isSocietyVouched = true,
                    speciality = "North Indian & Thalis",
                    society = "Sylvan County"
                ),
                ChefProfileEntity(
                    uid = "chef_vikram",
                    isChef = true,
                    chefStory = "Passionate about rich Awadhi and Hyderabadi culinary traditions. Slow-cooked dum biryanis and melt-in-mouth kebabs every weekend.",
                    mealsServedCount = 98,
                    regularsCount = 19,
                    ratingAvg = 4.8,
                    isSocietyVouched = true,
                    speciality = "Dum Biryani & Awadhi",
                    society = "Sylvan County"
                )
            )

            for (chef in mockChefs) {
                if (firestore != null) {
                    try {
                        firestore.collection("chefProfiles").document(chef.uid).set(chef.toFirestoreMap()).await()
                    } catch (e: Exception) {
                        android.util.Log.e("SeedData", "Error seeding chef ${chef.uid} to Firestore: ${e.message}")
                    }
                }
                appDao.insertChefProfile(chef)
            }

            val mockMenuItems = listOf(
                MenuItemEntity(
                    firestoreId = "menu_paneer_salad",
                    chefUid = "chef_priya",
                    chefName = "Priya Sharma",
                    chefFlat = "Wing A, Flat 304",
                    dishName = "Spicy Paneer Salad Bowl",
                    description = "Fresh malai paneer cubes with organic greens, bell peppers, cherry tomatoes, and spicy mint-cilantro vinaigrette. High-protein, wholesome.",
                    price = 249.0,
                    portionsAvailable = 8,
                    portionsBooked = 3,
                    isVeg = true,
                    photoUrl = "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=500",
                    cuisineTags = "Healthy, Salad, High-Protein",
                    mealType = "LUNCH",
                    deliveryWindow = "12:30 PM - 1:30 PM",
                    society = "Sylvan County",
                    isSoldOut = false
                ),
                MenuItemEntity(
                    firestoreId = "menu_punjabi_thali",
                    chefUid = "chef_priya",
                    chefName = "Priya Sharma",
                    chefFlat = "Wing A, Flat 304",
                    dishName = "Royal Amritsari Thali",
                    description = "Dal Makhani, Shahi Paneer, 3 Phulkas with pure ghee, Jeera Rice, Boondi Raita, and Gulab Jamun.",
                    price = 280.0,
                    portionsAvailable = 12,
                    portionsBooked = 6,
                    isVeg = true,
                    photoUrl = "https://images.unsplash.com/photo-1585937421612-70a008356fbe?w=500",
                    cuisineTags = "North Indian, Thali, Desi Ghee",
                    mealType = "LUNCH",
                    deliveryWindow = "1:00 PM - 2:00 PM",
                    society = "Sylvan County",
                    isSoldOut = false
                ),
                MenuItemEntity(
                    firestoreId = "menu_chicken_biryani",
                    chefUid = "chef_vikram",
                    chefName = "Vikram Malhotra",
                    chefFlat = "Wing C, Flat 902",
                    dishName = "Nawabi Chicken Biryani",
                    description = "Fragrant long-grain basmati rice slow-cooked with tender marinated chicken and secret ground spices, served with cooling burani raita and salan.",
                    price = 380.0,
                    portionsAvailable = 6,
                    portionsBooked = 4,
                    isVeg = false,
                    photoUrl = "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?w=500",
                    cuisineTags = "Awadhi, Biryani, Non-Veg",
                    mealType = "LUNCH",
                    deliveryWindow = "12:45 PM - 1:45 PM",
                    society = "Sylvan County",
                    isSoldOut = false
                )
            )

            for (item in mockMenuItems) {
                if (firestore != null) {
                    try {
                        firestore.collection("menuItems").document(item.firestoreId).set(item.toFirestoreMap()).await()
                    } catch (e: Exception) {
                        android.util.Log.e("SeedData", "Error seeding menu item ${item.firestoreId} to Firestore: ${e.message}")
                    }
                }
                appDao.insertMenuItem(item)
            }
        }
    }

    // --- MealHub Repository Operations ---
    fun getChefProfile(uid: String): Flow<ChefProfileEntity?> = appDao.getChefProfileByUid(uid)

    fun getAllChefsForSociety(society: String): Flow<List<ChefProfileEntity>> {
        return if (society.isEmpty() || society == "All Societies") {
            appDao.getAllChefsForSociety("")
        } else {
            appDao.getAllChefsForSociety(society)
        }
    }

    fun getMenuItemsForSociety(society: String): Flow<List<MenuItemEntity>> {
        return if (society.isEmpty() || society == "All Societies") {
            appDao.getAllMenuItems()
        } else {
            appDao.getAllMenuItemsForSociety(society)
        }
    }

    fun getMenuItemsForChef(chefUid: String): Flow<List<MenuItemEntity>> = appDao.getMenuItemsForChef(chefUid)

    fun getMenuItemById(id: Int): Flow<MenuItemEntity?> = appDao.getMenuItemById(id)

    suspend fun becomeChef(
        chefProfile: ChefProfileEntity,
        initialSpecial: MenuItemEntity
    ) {
        val insertedChefId = appDao.insertChefProfile(chefProfile)
        val insertedItemId = appDao.insertMenuItem(initialSpecial)

        val fs = firestore
        if (fs != null && chefProfile.uid.isNotEmpty()) {
            try {
                fs.collection("chefProfiles").document(chefProfile.uid).set(chefProfile.toFirestoreMap()).await()
                val docRef = fs.collection("menuItems").document()
                val updatedItem = initialSpecial.copy(id = insertedItemId.toInt(), firestoreId = docRef.id, pendingSync = false)
                docRef.set(updatedItem.toFirestoreMap()).await()
                appDao.updateMenuItem(updatedItem)
            } catch (e: Exception) {
                android.util.Log.e("MealHub", "Failed to sync chef profile: ${e.message}")
                appDao.updateChefProfile(chefProfile.copy(pendingSync = true))
                appDao.updateMenuItem(initialSpecial.copy(id = insertedItemId.toInt(), pendingSync = true))
                scheduleSyncJob()
            }
        } else {
            appDao.updateChefProfile(chefProfile.copy(pendingSync = true))
            appDao.updateMenuItem(initialSpecial.copy(id = insertedItemId.toInt(), pendingSync = true))
            scheduleSyncJob()
        }
    }

    suspend fun createMenuItem(item: MenuItemEntity): Long {
        val insertedId = appDao.insertMenuItem(item)
        var finalItem = item.copy(id = insertedId.toInt())
        val fs = firestore
        if (fs != null) {
            val docRef = fs.collection("menuItems").document()
            finalItem = finalItem.copy(firestoreId = docRef.id)
            try {
                docRef.set(finalItem.toFirestoreMap()).await()
                appDao.updateMenuItem(finalItem)
            } catch (e: Exception) {
                finalItem = finalItem.copy(pendingSync = true)
                appDao.updateMenuItem(finalItem)
                scheduleSyncJob()
            }
        } else {
            finalItem = finalItem.copy(pendingSync = true)
            appDao.updateMenuItem(finalItem)
            scheduleSyncJob()
        }
        return insertedId
    }

    suspend fun toggleMenuItemSoldOut(itemId: Int, isSoldOut: Boolean) {
        val existing = appDao.getMenuItemByIdDirect(itemId) ?: return
        val updated = existing.copy(isSoldOut = isSoldOut, timestamp = System.currentTimeMillis())
        appDao.updateMenuItem(updated)
        val fs = firestore
        if (fs != null && updated.firestoreId.isNotEmpty()) {
            try {
                fs.collection("menuItems").document(updated.firestoreId).update(
                    mapOf(
                        "isSoldOut" to isSoldOut,
                        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: Exception) {
                appDao.updateMenuItem(updated.copy(pendingSync = true))
                scheduleSyncJob()
            }
        } else {
            appDao.updateMenuItem(updated.copy(pendingSync = true))
            scheduleSyncJob()
        }
    }

    suspend fun createMealOrder(order: MealOrderEntity): Long {
        val insertedId = appDao.insertMealOrder(order)
        var finalOrder = order.copy(id = insertedId.toInt())
        val fs = firestore
        if (fs != null) {
            val docRef = fs.collection("mealOrders").document()
            finalOrder = finalOrder.copy(firestoreId = docRef.id)
            try {
                docRef.set(finalOrder.toFirestoreMap()).await()
                appDao.updateMealOrder(finalOrder)
            } catch (e: Exception) {
                finalOrder = finalOrder.copy(pendingSync = true)
                appDao.updateMealOrder(finalOrder)
                scheduleSyncJob()
            }
        } else {
            finalOrder = finalOrder.copy(pendingSync = true)
            appDao.updateMealOrder(finalOrder)
            scheduleSyncJob()
        }

        if (order.menuItemId > 0) {
            val menuItem = appDao.getMenuItemByIdDirect(order.menuItemId)
            if (menuItem != null) {
                val newPortionsBooked = menuItem.portionsBooked + order.servingSize
                val isSoldOut = newPortionsBooked >= menuItem.portionsAvailable
                val updatedItem = menuItem.copy(
                    portionsBooked = newPortionsBooked,
                    isSoldOut = isSoldOut,
                    pendingSync = false,
                    timestamp = System.currentTimeMillis()
                )
                appDao.updateMenuItem(updatedItem)
                if (fs != null && updatedItem.firestoreId.isNotEmpty()) {
                    try {
                        fs.collection("menuItems").document(updatedItem.firestoreId).update(
                            mapOf(
                                "portionsBooked" to newPortionsBooked,
                                "isSoldOut" to isSoldOut,
                                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                        ).await()
                    } catch (e: Exception) {
                        android.util.Log.e("MealHub", "Failed to sync menu item portion update: ${e.message}")
                        appDao.updateMenuItem(updatedItem.copy(pendingSync = true))
                        scheduleSyncJob()
                    }
                } else {
                    appDao.updateMenuItem(updatedItem.copy(pendingSync = true))
                    scheduleSyncJob()
                }
            }
        }

        return insertedId
    }

    suspend fun updateOrderStatus(orderId: Int, newStatus: String) {
        val existing = appDao.getOrderByIdDirect(orderId) ?: return
        val updated = existing.copy(status = newStatus, timestamp = System.currentTimeMillis())
        appDao.updateMealOrder(updated)
        val fs = firestore
        if (fs != null && updated.firestoreId.isNotEmpty()) {
            try {
                fs.collection("mealOrders").document(updated.firestoreId).update(
                    mapOf(
                        "status" to newStatus,
                        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: Exception) {
                appDao.updateMealOrder(updated.copy(pendingSync = true))
                scheduleSyncJob()
            }
        } else {
            appDao.updateMealOrder(updated.copy(pendingSync = true))
            scheduleSyncJob()
        }
    }

    fun getOrdersForBuyer(buyerUid: String): Flow<List<MealOrderEntity>> = appDao.getOrdersForBuyer(buyerUid)

    fun getOrdersForChef(chefUid: String): Flow<List<MealOrderEntity>> = appDao.getOrdersForChef(chefUid)

    suspend fun createMealSubscription(sub: MealSubscriptionEntity): Long {
        val insertedId = appDao.insertMealSubscription(sub)
        var finalSub = sub.copy(id = insertedId.toInt())
        val fs = firestore
        if (fs != null) {
            val docRef = fs.collection("mealSubscriptions").document()
            finalSub = finalSub.copy(firestoreId = docRef.id)
            try {
                docRef.set(finalSub.toFirestoreMap()).await()
                appDao.updateMealSubscription(finalSub)
            } catch (e: Exception) {
                finalSub = finalSub.copy(pendingSync = true)
                appDao.updateMealSubscription(finalSub)
                scheduleSyncJob()
            }
        } else {
            finalSub = finalSub.copy(pendingSync = true)
            appDao.updateMealSubscription(finalSub)
            scheduleSyncJob()
        }
        return insertedId
    }

    suspend fun toggleSubscriptionStatus(subId: Int, newStatus: String) {
        val existing = appDao.getSubscriptionByIdDirect(subId) ?: return
        val updated = existing.copy(status = newStatus, timestamp = System.currentTimeMillis())
        appDao.updateMealSubscription(updated)
        val fs = firestore
        if (fs != null && updated.firestoreId.isNotEmpty()) {
            try {
                fs.collection("mealSubscriptions").document(updated.firestoreId).update(
                    mapOf(
                        "status" to newStatus,
                        "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "serverTimestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()
            } catch (e: Exception) {
                appDao.updateMealSubscription(updated.copy(pendingSync = true))
                scheduleSyncJob()
            }
        } else {
            appDao.updateMealSubscription(updated.copy(pendingSync = true))
            scheduleSyncJob()
        }
    }

    fun getSubscriptionsForBuyer(buyerUid: String): Flow<List<MealSubscriptionEntity>> = appDao.getSubscriptionsForBuyer(buyerUid)

    fun getSubscriptionsForChef(chefUid: String): Flow<List<MealSubscriptionEntity>> = appDao.getSubscriptionsForChef(chefUid)
}
