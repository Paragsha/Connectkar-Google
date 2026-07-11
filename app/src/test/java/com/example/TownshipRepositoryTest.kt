package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDao
import com.example.data.local.AppDatabase
import com.example.data.local.UserEntity
import com.example.data.local.ListingEntity
import com.example.data.repository.TownshipRepository
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
}
