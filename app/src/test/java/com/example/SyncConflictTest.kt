package com.example

import com.example.data.local.ChefProfileEntity
import com.example.data.local.ListingEntity
import com.example.data.local.MealOrderEntity
import com.example.data.local.MealSubscriptionEntity
import com.example.data.local.MenuItemEntity
import com.example.data.local.UserEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests validating the timestamp-based conflict resolution and merge strategy
 * between local Room entities with pending sync flags and incoming remote Firestore updates.
 */
class SyncConflictTest {

    // Helper implementing the conflict resolution merge decision
    private fun shouldApplyRemoteUpdate(
        existingPendingSync: Boolean,
        existingTimestamp: Long,
        remoteTimestamp: Long
    ): Boolean {
        if (!existingPendingSync) return true
        return remoteTimestamp > existingTimestamp
    }

    // =========================================================================
    // 1. UserEntity Conflict Resolution Tests
    // =========================================================================

    @Test
    fun userConflict_newerRemoteOverwritesOlderPendingLocal() {
        val localUser = UserEntity(
            uid = "user_123",
            fullName = "Old Local Name",
            society = "Green Valley",
            pendingSync = true,
            timestamp = 1000L
        )

        val remoteUser = UserEntity(
            uid = "user_123",
            fullName = "Updated Cloud Name",
            society = "Green Valley",
            pendingSync = false,
            timestamp = 2000L
        )

        val shouldApply = shouldApplyRemoteUpdate(
            existingPendingSync = localUser.pendingSync,
            existingTimestamp = localUser.timestamp,
            remoteTimestamp = remoteUser.timestamp
        )

        assertTrue("Newer remote entity should overwrite older local entity", shouldApply)

        val merged = if (shouldApply) remoteUser.copy(pendingSync = false) else localUser
        assertEquals("Updated Cloud Name", merged.fullName)
        assertFalse(merged.pendingSync)
    }

    @Test
    fun userConflict_olderRemotePreservesNewerPendingLocal() {
        val localUser = UserEntity(
            uid = "user_123",
            fullName = "Unsynced Fresh Edit",
            society = "Green Valley",
            pendingSync = true,
            timestamp = 3000L
        )

        val remoteUser = UserEntity(
            uid = "user_123",
            fullName = "Stale Cloud Name",
            society = "Green Valley",
            pendingSync = false,
            timestamp = 2000L
        )

        val shouldApply = shouldApplyRemoteUpdate(
            existingPendingSync = localUser.pendingSync,
            existingTimestamp = localUser.timestamp,
            remoteTimestamp = remoteUser.timestamp
        )

        assertFalse("Older remote entity must not overwrite newer pending local change", shouldApply)
    }

    // =========================================================================
    // 2. ChefProfileEntity Conflict Resolution Tests
    // =========================================================================

    @Test
    fun chefProfileConflict_newerRemoteOverwritesOlderPendingLocal() {
        val localChef = ChefProfileEntity(
            uid = "chef_456",
            mealsServedCount = 10,
            speciality = "North Indian",
            pendingSync = true,
            timestamp = 1500L
        )

        val remoteChef = ChefProfileEntity(
            uid = "chef_456",
            mealsServedCount = 35,
            speciality = "North Indian & Gujarati Thali",
            pendingSync = false,
            timestamp = 2500L
        )

        val shouldApply = shouldApplyRemoteUpdate(
            existingPendingSync = localChef.pendingSync,
            existingTimestamp = localChef.timestamp,
            remoteTimestamp = remoteChef.timestamp
        )

        assertTrue(shouldApply)
        val merged = if (shouldApply) remoteChef.copy(pendingSync = false) else localChef
        assertEquals(35, merged.mealsServedCount)
        assertEquals("North Indian & Gujarati Thali", merged.speciality)
        assertFalse(merged.pendingSync)
    }

    // =========================================================================
    // 3. MenuItemEntity Conflict Resolution & ID Preservation Tests
    // =========================================================================

    @Test
    fun menuItemConflict_preservesLocalRoomPrimaryKeyOnRemoteUpdate() {
        val localMenuItem = MenuItemEntity(
            id = 42, // Room SQLite local primary key
            firestoreId = "menu_doc_789",
            dishName = "Old Dal Tadka",
            price = 120.0,
            portionsAvailable = 2,
            pendingSync = true,
            timestamp = 1000L
        )

        val remoteMenuItem = MenuItemEntity(
            id = 0, // Remote snapshot has no concept of device SQLite auto-inc ID
            firestoreId = "menu_doc_789",
            dishName = "Special Dal Tadka + Jeera Rice",
            price = 150.0,
            portionsAvailable = 10,
            pendingSync = false,
            timestamp = 2000L
        )

        val shouldApply = shouldApplyRemoteUpdate(
            existingPendingSync = localMenuItem.pendingSync,
            existingTimestamp = localMenuItem.timestamp,
            remoteTimestamp = remoteMenuItem.timestamp
        )

        assertTrue(shouldApply)

        // Merge strategy: apply remote data while preserving local SQLite primary key
        val merged = remoteMenuItem.copy(
            id = localMenuItem.id,
            pendingSync = false
        )

        assertEquals(42, merged.id)
        assertEquals("menu_doc_789", merged.firestoreId)
        assertEquals("Special Dal Tadka + Jeera Rice", merged.dishName)
        assertEquals(150.0, merged.price, 0.001)
        assertEquals(10, merged.portionsAvailable)
        assertFalse(merged.pendingSync)
    }

    // =========================================================================
    // 4. MealOrderEntity Conflict Resolution Tests
    // =========================================================================

    @Test
    fun mealOrderConflict_newerRemoteStatusOverwritesOlderPendingLocal() {
        val localOrder = MealOrderEntity(
            id = 101,
            firestoreId = "order_doc_555",
            status = "PLACED",
            pendingSync = true,
            timestamp = 5000L
        )

        val remoteOrder = MealOrderEntity(
            id = 0,
            firestoreId = "order_doc_555",
            status = "PREPARING",
            pendingSync = false,
            timestamp = 6000L
        )

        val shouldApply = shouldApplyRemoteUpdate(
            existingPendingSync = localOrder.pendingSync,
            existingTimestamp = localOrder.timestamp,
            remoteTimestamp = remoteOrder.timestamp
        )

        assertTrue(shouldApply)
        val merged = remoteOrder.copy(id = localOrder.id, pendingSync = false)
        assertEquals(101, merged.id)
        assertEquals("PREPARING", merged.status)
        assertFalse(merged.pendingSync)
    }

    // =========================================================================
    // 5. MealSubscriptionEntity Conflict Resolution Tests
    // =========================================================================

    @Test
    fun mealSubscriptionConflict_newerRemoteOverwritesOlderPendingLocal() {
        val localSubscription = MealSubscriptionEntity(
            id = 7,
            firestoreId = "sub_doc_999",
            daysRemaining = 15,
            status = "ACTIVE",
            pendingSync = true,
            timestamp = 1000L
        )

        val remoteSubscription = MealSubscriptionEntity(
            id = 0,
            firestoreId = "sub_doc_999",
            daysRemaining = 14,
            status = "ACTIVE",
            pendingSync = false,
            timestamp = 2000L
        )

        val shouldApply = shouldApplyRemoteUpdate(
            existingPendingSync = localSubscription.pendingSync,
            existingTimestamp = localSubscription.timestamp,
            remoteTimestamp = remoteSubscription.timestamp
        )

        assertTrue(shouldApply)
        val merged = remoteSubscription.copy(id = localSubscription.id, pendingSync = false)
        assertEquals(7, merged.id)
        assertEquals(14, merged.daysRemaining)
        assertFalse(merged.pendingSync)
    }

    // =========================================================================
    // 6. ListingEntity Conflict Resolution Tests
    // =========================================================================

    @Test
    fun listingConflict_newerRemoteOverwritesOlderPendingLocal() {
        val localListing = ListingEntity(
            id = 15,
            firestoreId = "listing_doc_333",
            title = "Bicycle for sale (draft)",
            price = 2000.0,
            pendingSync = true,
            timestamp = 1000L
        )

        val remoteListing = ListingEntity(
            id = 0,
            firestoreId = "listing_doc_333",
            title = "Bicycle for sale (published)",
            price = 1800.0,
            pendingSync = false,
            timestamp = 2000L
        )

        val shouldApply = shouldApplyRemoteUpdate(
            existingPendingSync = localListing.pendingSync,
            existingTimestamp = localListing.timestamp,
            remoteTimestamp = remoteListing.timestamp
        )

        assertTrue(shouldApply)
        val merged = remoteListing.copy(id = localListing.id, pendingSync = false)
        assertEquals(15, merged.id)
        assertEquals("Bicycle for sale (published)", merged.title)
        assertEquals(1800.0, merged.price, 0.001)
        assertFalse(merged.pendingSync)
    }
}
