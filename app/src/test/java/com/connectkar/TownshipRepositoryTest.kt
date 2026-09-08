package com.connectkar

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.connectkar.data.local.AppDao
import com.connectkar.data.local.AppDatabase
import com.connectkar.data.local.UserEntity
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.MenuItemEntity
import com.connectkar.data.local.MealOrderEntity
import com.connectkar.data.repository.TownshipRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TownshipRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        try {
            val config = androidx.work.Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor())
                .build()
            androidx.work.WorkManager.initialize(context, config)
        } catch (e: Exception) {
            // Already initialized or exception ignored
        }

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testConflictResolution_forUsers_onlyUpdatesWhenNotPendingSync() = runBlocking {
        // Given: A local user that has pending changes to sync
        val localUserPending = UserEntity(
            id = 1,
            uid = "user_123",
            fullName = "Local Unsynced User Name",
            phoneNumber = "1234567890",
            society = "Greenwood",
            blockTower = "A",
            flatNumber = "101",
            pendingSync = true,
            isCurrent = true
        )
        dao.insertUser(localUserPending)

        // When: An update comes from firestore with old or new name
        val firestoreUser = UserEntity(
            uid = "user_123",
            fullName = "Remote Overwrite Attempt",
            phoneNumber = "1234567890",
            society = "Greenwood",
            blockTower = "A",
            flatNumber = "101"
        )
        
        // Simulating the logic inside the users listener:
        val existing = dao.getUserByUidDirect(firestoreUser.uid)
        assertNotNull(existing)
        if (existing != null) {
            if (!existing.pendingSync) {
                dao.updateUser(firestoreUser.copy(id = existing.id, isCurrent = existing.isCurrent))
            }
        }

        // Then: Local unsynced name is preserved, REMOTE attempt is rejected
        val finalUser = dao.getUserByUidDirect("user_123")
        assertNotNull(finalUser)
        assertEquals("Local Unsynced User Name", finalUser?.fullName)
        assertTrue(finalUser?.pendingSync == true)
    }

    @Test
    fun testConflictResolution_forUsers_updatesWhenNotPendingSync() = runBlocking {
        // Given: A local user that has NO pending changes to sync
        val localUserSynced = UserEntity(
            id = 1,
            uid = "user_123",
            fullName = "Local Synced Name",
            phoneNumber = "1234567890",
            society = "Greenwood",
            blockTower = "A",
            flatNumber = "101",
            pendingSync = false,
            isCurrent = true
        )
        dao.insertUser(localUserSynced)

        // When: An update comes from firestore
        val firestoreUser = UserEntity(
            uid = "user_123",
            fullName = "Remote Overwrite",
            phoneNumber = "1234567890",
            society = "Greenwood",
            blockTower = "A",
            flatNumber = "101"
        )

        // Simulating the users listener logic:
        val existing = dao.getUserByUidDirect(firestoreUser.uid)
        assertNotNull(existing)
        if (existing != null) {
            if (!existing.pendingSync) {
                dao.updateUser(firestoreUser.copy(id = existing.id, isCurrent = existing.isCurrent))
            }
        }

        // Then: Local database user is updated to "Remote Overwrite"
        val finalUser = dao.getUserByUidDirect("user_123")
        assertNotNull(finalUser)
        assertEquals("Remote Overwrite", finalUser?.fullName)
        assertFalse(finalUser?.pendingSync == true)
    }

    @Test
    fun testConflictResolution_forListings_ignoresOlderRemoteUpdatesWhenLocalIsPendingSync() = runBlocking {
        // Given: A local listing with pending local edits and timestamp 1000
        val localListing = ListingEntity(
            id = 1,
            firestoreId = "list_456",
            title = "Awesome Local Edit",
            description = "Unsynced local",
            type = "MARKETPLACE",
            category = "Books",
            price = 25.0,
            society = "Greenwood",
            timestamp = 1000L,
            pendingSync = true
        )
        dao.insertListing(localListing)

        // When: A remote listing snapshot is received with an older timestamp (e.g. 500)
        val remoteListingOld = ListingEntity(
            firestoreId = "list_456",
            title = "Outdated Remote Title",
            description = "Remote version",
            type = "MARKETPLACE",
            category = "Books",
            price = 20.0,
            society = "Greenwood",
            timestamp = 500L
        )

        // Simulating listings listener conflict resolution:
        val existing = dao.getListingByFirestoreIdDirect(remoteListingOld.firestoreId)
        assertNotNull(existing)
        if (existing != null) {
            if (existing.pendingSync) {
                if (remoteListingOld.timestamp > existing.timestamp) {
                    dao.updateListing(remoteListingOld.copy(id = existing.id))
                }
            } else {
                dao.updateListing(remoteListingOld.copy(id = existing.id))
            }
        }

        // Then: Local title "Awesome Local Edit" is preserved
        val finalListing = dao.getListingByFirestoreIdDirect("list_456")
        assertNotNull(finalListing)
        assertEquals("Awesome Local Edit", finalListing?.title)
    }

    @Test
    fun testConflictResolution_forListings_acceptsNewerRemoteUpdatesEvenIfLocalIsPendingSync() = runBlocking {
        // Given: A local listing with pending local edits and timestamp 1000
        val localListing = ListingEntity(
            id = 1,
            firestoreId = "list_456",
            title = "Awesome Local Edit",
            description = "Unsynced local",
            type = "MARKETPLACE",
            category = "Books",
            price = 25.0,
            society = "Greenwood",
            timestamp = 1000L,
            pendingSync = true
        )
        dao.insertListing(localListing)

        // When: A remote listing snapshot is received with a newer timestamp (e.g. 2000)
        val remoteListingNew = ListingEntity(
            firestoreId = "list_456",
            title = "Newer Remote Title",
            description = "Remote version",
            type = "MARKETPLACE",
            category = "Books",
            price = 30.0,
            society = "Greenwood",
            timestamp = 2000L
        )

        // Simulating listings listener conflict resolution:
        val existing = dao.getListingByFirestoreIdDirect(remoteListingNew.firestoreId)
        assertNotNull(existing)
        if (existing != null) {
            if (existing.pendingSync) {
                if (remoteListingNew.timestamp > existing.timestamp) {
                    dao.updateListing(remoteListingNew.copy(id = existing.id))
                }
            } else {
                dao.updateListing(remoteListingNew.copy(id = existing.id))
            }
        }

        // Then: Local listing is updated since remote is newer
        val finalListing = dao.getListingByFirestoreIdDirect("list_456")
        assertNotNull(finalListing)
        assertEquals("Newer Remote Title", finalListing?.title)
    }

    @Test
    fun testCreateMealOrder_incrementsPortionsBooked_andUpdatesSoldOut() = runBlocking {
        val repository = TownshipRepository(dao, context, null)

        val initialItem = MenuItemEntity(
            id = 1,
            firestoreId = "item_101",
            dishName = "Dal Makhani with Jeera Rice",
            portionsAvailable = 5,
            portionsBooked = 1,
            isSoldOut = false
        )
        dao.insertMenuItem(initialItem)

        val order = MealOrderEntity(
            menuItemId = 1,
            dishName = "Dal Makhani with Jeera Rice",
            servingSize = 2,
            grandTotal = 300.0,
            buyerUid = "buyer_1"
        )
        repository.createMealOrder(order)

        val updatedItem = dao.getMenuItemByIdDirect(1)
        assertNotNull(updatedItem)
        assertEquals("Portions booked should increase from 1 to 3", 3, updatedItem?.portionsBooked)
        assertFalse("Item should not be sold out yet (3/5 booked)", updatedItem?.isSoldOut ?: true)

        // Place another order that completes available portions
        val secondOrder = MealOrderEntity(
            menuItemId = 1,
            dishName = "Dal Makhani with Jeera Rice",
            servingSize = 2,
            grandTotal = 300.0,
            buyerUid = "buyer_2"
        )
        repository.createMealOrder(secondOrder)

        val soldOutItem = dao.getMenuItemByIdDirect(1)
        assertNotNull(soldOutItem)
        assertEquals("Portions booked should now be 5", 5, soldOutItem?.portionsBooked)
        assertTrue("Item should be marked sold out when portionsBooked >= portionsAvailable", soldOutItem?.isSoldOut ?: false)
    }

    @Test
    fun testUpdateVerificationStatus_setsPendingSync_whenRemoteCallThrows() = runBlocking {
        val repository = TownshipRepository(dao, context, null)

        val unverifiedUser = UserEntity(
            id = 1,
            uid = "user_999",
            fullName = "Pending Approval Resident",
            phoneNumber = "9876543210",
            society = "Greenwood",
            blockTower = "Block B",
            flatNumber = "202",
            isVerified = false,
            isPending = true,
            pendingSync = false
        )
        dao.insertUser(unverifiedUser)

        // When updating verification status where Firestore/Functions are not reachable
        repository.updateVerificationStatus(1, isVerified = true)

        val updatedUser = dao.getUserById(1)
        assertNotNull(updatedUser)
        assertTrue("User should be optimistically marked verified", updatedUser?.isVerified == true)
        assertFalse("User should no longer be isPending", updatedUser?.isPending == true)
        assertTrue("User must be marked pendingSync = true so SyncWorker will retry remote update", updatedUser?.pendingSync == true)
    }

    @Test
    fun testGetListingsByAuthor_and_getBookmarkedListingsByType() = runBlocking {
        val repository = TownshipRepository(dao, context, null)

        val listing1 = ListingEntity(
            id = 1,
            type = "PROPERTY",
            title = "2 BHK Apartment",
            description = "Spacious 2 BHK",
            price = 25000.0,
            authorUid = "author_1",
            isBookmarked = true,
            isDraft = false
        )
        val listing2 = ListingEntity(
            id = 2,
            type = "PROPERTY",
            title = "3 BHK Villa",
            description = "Luxury 3 BHK Villa",
            price = 45000.0,
            authorUid = "author_2",
            isBookmarked = false,
            isDraft = false
        )
        val listing3 = ListingEntity(
            id = 3,
            type = "MARKETPLACE",
            title = "Dining Table",
            description = "Wooden dining table",
            price = 5000.0,
            authorUid = "author_1",
            isBookmarked = true,
            isDraft = false
        )

        dao.insertListing(listing1)
        dao.insertListing(listing2)
        dao.insertListing(listing3)

        // Test getListingsByAuthor
        val author1Listings = repository.getListingsByAuthor("author_1").first()
        assertEquals(2, author1Listings.size)
        assertTrue(author1Listings.any { it.id == 1 })
        assertTrue(author1Listings.any { it.id == 3 })

        val author2Listings = repository.getListingsByAuthor("author_2").first()
        assertEquals(1, author2Listings.size)
        assertEquals(2, author2Listings[0].id)

        // Test getBookmarkedListingsByType for PROPERTY
        val bookmarkedProperties = repository.getBookmarkedListingsByType("PROPERTY").first()
        assertEquals(1, bookmarkedProperties.size)
        assertEquals(1, bookmarkedProperties[0].id)

        // Test getBookmarkedListingsByType for MARKETPLACE
        val bookmarkedMarketplace = repository.getBookmarkedListingsByType("MARKETPLACE").first()
        assertEquals(1, bookmarkedMarketplace.size)
        assertEquals(3, bookmarkedMarketplace[0].id)
    }

    @Test
    fun testToggleLikeAndBookmarkListing_setsPendingSync_whenOffline() = runBlocking {
        val repository = TownshipRepository(dao, context, null)

        val listing = ListingEntity(
            id = 10,
            type = "MARKETPLACE",
            title = "Bicycle",
            description = "Good condition bike",
            price = 3000.0,
            authorUid = "user_1",
            isLikedByMe = false,
            likesCount = 2,
            isBookmarked = false,
            pendingSync = false,
            isDraft = false
        )
        dao.insertListing(listing)

        // Toggle like
        repository.toggleLikeListing(10)
        val likedListing = dao.getListingById(10)
        assertNotNull(likedListing)
        assertTrue(likedListing?.isLikedByMe == true)
        assertEquals(3, likedListing?.likesCount)
        assertTrue("Must be marked pendingSync = true for background worker retry", likedListing?.pendingSync == true)

        // Reset pendingSync
        dao.updateListing(likedListing!!.copy(pendingSync = false))

        // Toggle bookmark
        repository.toggleBookmarkListing(10)
        val bookmarkedListing = dao.getListingById(10)
        assertNotNull(bookmarkedListing)
        assertTrue(bookmarkedListing?.isBookmarked == true)
        assertTrue("Must be marked pendingSync = true for background worker retry", bookmarkedListing?.pendingSync == true)
    }

    @Test
    fun testPerUserListingInteractions_separateUsersDoNotOverwriteEachOther() = runBlocking {
        val listing = ListingEntity(
            id = 20,
            type = "PROPERTY",
            title = "Luxury Condo",
            description = "High floor view",
            price = 60000.0,
            authorUid = "landlord_1",
            likesCount = 5
        )
        dao.insertListing(listing)

        // User A likes and bookmarks
        dao.setLiked("user_resident_a", 20, true)
        dao.setBookmarked("user_resident_a", 20, true)

        // User B only likes, does not bookmark
        dao.setLiked("user_resident_b", 20, true)
        dao.setBookmarked("user_resident_b", 20, false)

        val interactionA = dao.getInteraction("user_resident_a", 20)
        val interactionB = dao.getInteraction("user_resident_b", 20)

        assertNotNull(interactionA)
        assertNotNull(interactionB)

        assertTrue("User A must have liked = true", interactionA!!.isLiked)
        assertTrue("User A must have bookmarked = true", interactionA.isBookmarked)

        assertTrue("User B must have liked = true", interactionB!!.isLiked)
        assertFalse("User B must have bookmarked = false", interactionB.isBookmarked)

        val bookmarkedA = dao.getBookmarkedListingIds("user_resident_a", "PROPERTY").first()
        val bookmarkedB = dao.getBookmarkedListingIds("user_resident_b", "PROPERTY").first()

        assertTrue(bookmarkedA.contains(20))
        assertFalse(bookmarkedB.contains(20))
    }
}
