package com.example.data.local

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

    @Query("SELECT * FROM listings WHERE society = :society ORDER BY timestamp DESC")
    fun getListingsBySociety(society: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE type = :type AND (society = :society OR society = '') ORDER BY timestamp DESC")
    fun getListingsByTypeAndSociety(type: String, society: String): Flow<List<ListingEntity>>

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
}
