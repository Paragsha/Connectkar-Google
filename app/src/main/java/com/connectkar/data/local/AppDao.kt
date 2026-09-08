package com.connectkar.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- User Operations ---
    @Query("SELECT * FROM users ORDER BY id DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrentUserDirect(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isCurrent = 0")
    suspend fun clearCurrentUserFlag()

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): UserEntity?

    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    suspend fun getUserByUidDirect(uid: String): UserEntity?

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)


    // --- Listing Operations ---
    @Query("SELECT * FROM listings ORDER BY timestamp DESC")
    fun getAllListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE type = :type ORDER BY timestamp DESC")
    fun getListingsByType(type: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE authorUid = :uid ORDER BY timestamp DESC")
    fun getListingsByAuthor(uid: String): Flow<List<ListingEntity>>

    @Query("""
        SELECT l.* FROM listings l
        LEFT JOIN user_listing_interactions uli ON l.id = uli.listingId AND uli.userId = (SELECT uid FROM users WHERE isCurrent = 1 LIMIT 1)
        WHERE (uli.isBookmarked = 1 OR (uli.userId IS NULL AND l.isBookmarked = 1))
          AND l.type = :type AND l.isDraft = 0
        ORDER BY l.timestamp DESC
    """)
    fun getBookmarkedListingsByType(type: String): Flow<List<ListingEntity>>

    @Query("""
        SELECT l.* FROM listings l
        INNER JOIN user_listing_interactions uli ON l.id = uli.listingId
        WHERE uli.userId = :userId AND uli.isBookmarked = 1 AND l.type = :type AND l.isDraft = 0
        ORDER BY l.timestamp DESC
    """)
    fun getBookmarkedListingsByType(type: String, userId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE society = :society OR isPublic = 1 ORDER BY timestamp DESC")
    fun getListingsBySociety(society: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE type = :type AND (society = :society OR society = '' OR isPublic = 1) ORDER BY timestamp DESC")
    fun getListingsByTypeAndSociety(type: String, society: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE isDraft = 1 AND type = :type LIMIT 1")
    suspend fun getDraftListingByTypeDirect(type: String): ListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity): Long

    @Update
    suspend fun updateListing(listing: ListingEntity)

    @Query("DELETE FROM listings WHERE id = :id")
    suspend fun deleteListingById(id: Int)

    @Query("SELECT * FROM listings WHERE id = :id")
    suspend fun getListingById(id: Int): ListingEntity?

    @Query("SELECT * FROM listings WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getListingByFirestoreIdDirect(firestoreId: String): ListingEntity?

    @Query("SELECT * FROM listings WHERE pendingSync = 1 ORDER BY timestamp ASC")
    suspend fun getUnsyncedListings(): List<ListingEntity>

    @Query("SELECT * FROM users WHERE pendingSync = 1 ORDER BY id ASC")
    suspend fun getUnsyncedUsers(): List<UserEntity>

    // --- Chef Profile Operations ---
    @Query("SELECT * FROM chef_profiles WHERE uid = :uid LIMIT 1")
    fun getChefProfileByUid(uid: String): Flow<ChefProfileEntity?>

    @Query("SELECT * FROM chef_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getChefProfileByUidDirect(uid: String): ChefProfileEntity?

    @Query("SELECT * FROM chef_profiles WHERE society = :society OR society = '' ORDER BY mealsServedCount DESC")
    fun getAllChefsForSociety(society: String): Flow<List<ChefProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChefProfile(chef: ChefProfileEntity): Long

    @Update
    suspend fun updateChefProfile(chef: ChefProfileEntity)

    @Query("SELECT * FROM chef_profiles WHERE pendingSync = 1")
    suspend fun getUnsyncedChefProfiles(): List<ChefProfileEntity>

    // --- Menu Item Operations ---
    @Query("SELECT * FROM menu_items WHERE society = :society OR society = '' ORDER BY timestamp DESC")
    fun getAllMenuItemsForSociety(society: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items ORDER BY timestamp DESC")
    fun getAllMenuItems(): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE chefUid = :chefUid ORDER BY timestamp DESC")
    fun getMenuItemsForChef(chefUid: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    fun getMenuItemById(id: Int): Flow<MenuItemEntity?>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun getMenuItemByIdDirect(id: Int): MenuItemEntity?

    @Query("SELECT * FROM menu_items WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getMenuItemByFirestoreIdDirect(firestoreId: String): MenuItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: MenuItemEntity): Long

    @Update
    suspend fun updateMenuItem(item: MenuItemEntity)

    @Query("DELETE FROM menu_items WHERE id = :id")
    suspend fun deleteMenuItemById(id: Int)

    @Query("SELECT * FROM menu_items WHERE pendingSync = 1 ORDER BY timestamp ASC")
    suspend fun getUnsyncedMenuItems(): List<MenuItemEntity>

    // --- Meal Order Operations ---
    @Query("SELECT * FROM meal_orders WHERE buyerUid = :buyerUid ORDER BY timestamp DESC")
    fun getOrdersForBuyer(buyerUid: String): Flow<List<MealOrderEntity>>

    @Query("SELECT * FROM meal_orders WHERE chefUid = :chefUid ORDER BY timestamp DESC")
    fun getOrdersForChef(chefUid: String): Flow<List<MealOrderEntity>>

    @Query("SELECT * FROM meal_orders WHERE id = :id LIMIT 1")
    fun getOrderById(id: Int): Flow<MealOrderEntity?>

    @Query("SELECT * FROM meal_orders WHERE id = :id LIMIT 1")
    suspend fun getOrderByIdDirect(id: Int): MealOrderEntity?

    @Query("SELECT * FROM meal_orders WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getOrderByFirestoreIdDirect(firestoreId: String): MealOrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealOrder(order: MealOrderEntity): Long

    @Update
    suspend fun updateMealOrder(order: MealOrderEntity)

    @Query("SELECT * FROM meal_orders WHERE pendingSync = 1 ORDER BY timestamp ASC")
    suspend fun getUnsyncedMealOrders(): List<MealOrderEntity>

    // --- Meal Subscription Operations ---
    @Query("SELECT * FROM meal_subscriptions WHERE buyerUid = :buyerUid ORDER BY timestamp DESC")
    fun getSubscriptionsForBuyer(buyerUid: String): Flow<List<MealSubscriptionEntity>>

    @Query("SELECT * FROM meal_subscriptions WHERE chefUid = :chefUid ORDER BY timestamp DESC")
    fun getSubscriptionsForChef(chefUid: String): Flow<List<MealSubscriptionEntity>>

    @Query("SELECT * FROM meal_subscriptions WHERE id = :id LIMIT 1")
    suspend fun getSubscriptionByIdDirect(id: Int): MealSubscriptionEntity?

    @Query("SELECT * FROM meal_subscriptions WHERE firestoreId = :firestoreId LIMIT 1")
    suspend fun getSubscriptionByFirestoreIdDirect(firestoreId: String): MealSubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealSubscription(sub: MealSubscriptionEntity): Long

    @Update
    suspend fun updateMealSubscription(sub: MealSubscriptionEntity)

    @Query("SELECT * FROM meal_subscriptions WHERE pendingSync = 1 ORDER BY timestamp ASC")
    suspend fun getUnsyncedMealSubscriptions(): List<MealSubscriptionEntity>

    // --- User Listing Interaction Operations ---
    @Query("SELECT * FROM user_listing_interactions WHERE userId = :userId AND listingId = :listingId LIMIT 1")
    suspend fun getInteraction(userId: String, listingId: Int): UserListingInteractionEntity?

    @Query("SELECT * FROM user_listing_interactions WHERE userId = :userId AND listingId = :listingId LIMIT 1")
    fun getInteractionFlow(userId: String, listingId: Int): Flow<UserListingInteractionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: UserListingInteractionEntity)

    @Transaction
    suspend fun setLiked(userId: String, listingId: Int, isLiked: Boolean) {
        val existing = getInteraction(userId, listingId)
        if (existing != null) {
            insertInteraction(existing.copy(isLiked = isLiked))
        } else {
            insertInteraction(UserListingInteractionEntity(userId = userId, listingId = listingId, isLiked = isLiked, isBookmarked = false))
        }
    }

    @Transaction
    suspend fun setBookmarked(userId: String, listingId: Int, isBookmarked: Boolean) {
        val existing = getInteraction(userId, listingId)
        if (existing != null) {
            insertInteraction(existing.copy(isBookmarked = isBookmarked))
        } else {
            insertInteraction(UserListingInteractionEntity(userId = userId, listingId = listingId, isLiked = false, isBookmarked = isBookmarked))
        }
    }

    @Query("""
        SELECT l.id FROM listings l
        INNER JOIN user_listing_interactions uli ON l.id = uli.listingId
        WHERE uli.userId = :userId AND uli.isBookmarked = 1 AND l.type = :type AND l.isDraft = 0
        ORDER BY l.timestamp DESC
    """)
    fun getBookmarkedListingIds(userId: String, type: String): Flow<List<Int>>
}
