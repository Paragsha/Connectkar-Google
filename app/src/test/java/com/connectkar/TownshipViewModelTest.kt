package com.connectkar

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.connectkar.data.local.AppDao
import com.connectkar.data.local.AppDatabase
import com.connectkar.data.local.UserEntity
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.repository.TownshipRepository
import com.connectkar.ui.TownshipViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TownshipViewModelTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao
    private lateinit var context: Context
    private lateinit var repository: TownshipRepository
    private lateinit var viewModel: TownshipViewModel

    private val testDispatcher = StandardTestDispatcher()

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
        repository = TownshipRepository(dao, context, firestore = null)
        
        Dispatchers.setMain(testDispatcher)
        viewModel = TownshipViewModel(repository)
    }

    @After
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    @Test
    fun testDefaultStates() = runTest {
        assertEquals("All Societies", viewModel.selectedSociety.value)
        assertEquals("MARKETPLACE", viewModel.activeModule.value)
    }

    @Test
    fun testSelectSociety_updatesStateFlow() = runTest {
        viewModel.selectSociety("Greenwood")
        assertEquals("Greenwood", viewModel.selectedSociety.value)
    }

    @Test
    fun testSetActiveModule_updatesStateFlow() = runTest {
        viewModel.setActiveModule("PROPERTY")
        assertEquals("PROPERTY", viewModel.activeModule.value)
    }

    @Test
    fun testFilteredListings_reactsToSelectedSocietyAndActiveModule() = runTest {
        // Collect StateFlow in background to keep subscription active
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.filteredListings.collect {}
        }

        // Insert sample listings
        val listing1 = ListingEntity(
            id = 1,
            title = "Book for sale",
            type = "MARKETPLACE",
            description = "Some description",
            society = "Greenwood",
            timestamp = 100L
        )
        val listing2 = ListingEntity(
            id = 2,
            title = "Car rental",
            type = "VEHICLE",
            description = "Some description",
            society = "Greenwood",
            timestamp = 200L
        )
        val listing3 = ListingEntity(
            id = 3,
            title = "Apartment for rent",
            type = "PROPERTY",
            description = "Some description",
            society = "Sylvan County",
            timestamp = 300L
        )
        dao.insertListing(listing1)
        dao.insertListing(listing2)
        dao.insertListing(listing3)

        // Advance dispatcher to allow repository changes to propagate
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Default Module: MARKETPLACE, Society: All Societies
        viewModel.setActiveModule("MARKETPLACE")
        viewModel.selectSociety("All Societies")
        
        var filtered = viewModel.filteredListings.first { it.isNotEmpty() }
        assertEquals(1, filtered.size)
        assertEquals("Book for sale", filtered[0].title)

        // 2. Change Module to VEHICLE
        viewModel.setActiveModule("VEHICLE")
        filtered = viewModel.filteredListings.first { it.any { l -> l.type == "VEHICLE" } }
        assertEquals(1, filtered.size)
        assertEquals("Car rental", filtered[0].title)

        // 3. Change Society filter to Sylvan County (Module still VEHICLE)
        viewModel.selectSociety("Sylvan County")
        filtered = viewModel.filteredListings.first { it.isEmpty() }
        assertTrue(filtered.isEmpty())

        // 4. Change Module to PROPERTY (Society is Sylvan County)
        viewModel.setActiveModule("PROPERTY")
        filtered = viewModel.filteredListings.first { it.any { l -> l.type == "PROPERTY" } }
        assertEquals(1, filtered.size)
        assertEquals("Apartment for rent", filtered[0].title)

        collectJob.cancel()
    }

    @Test
    fun testApproveResident_updatesDatabaseVerificationState() = runTest {
        // Start collection on allUsers to activate SharingStarted.WhileSubscribed
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.allUsers.collect {}
        }

        val resident = UserEntity(
            id = 0,
            uid = "user_abc",
            fullName = "Jane Doe",
            phoneNumber = "9876543210",
            society = "Sylvan County",
            blockTower = "Block B",
            flatNumber = "402",
            isVerified = false,
            isPending = true
        )
        val generatedId = dao.insertUser(resident).toInt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Call VM approve action
        viewModel.approveUser(generatedId)
        
        // Wait for update to reflect in StateFlow
        val updatedList = viewModel.allUsers.first { users ->
            users.any { it.id == generatedId && it.isVerified && !it.isPending }
        }
        val updatedUser = updatedList.first { it.id == generatedId }
        assertTrue(updatedUser.isVerified)
        assertFalse(updatedUser.isPending)

        collectJob.cancel()
    }

    @Test
    fun testRejectResident_updatesDatabaseVerificationState() = runTest {
        // Start collection on allUsers to activate SharingStarted.WhileSubscribed
        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.allUsers.collect {}
        }

        val resident = UserEntity(
            id = 0,
            uid = "user_xyz",
            fullName = "John Doe",
            phoneNumber = "1112223334",
            society = "Sylvan County",
            blockTower = "Block C",
            flatNumber = "104",
            isVerified = true,
            isPending = false
        )
        val generatedId = dao.insertUser(resident).toInt()
        testDispatcher.scheduler.advanceUntilIdle()

        // Call VM reject action
        viewModel.rejectUser(generatedId)
        
        // Wait for update to reflect in StateFlow
        val updatedList = viewModel.allUsers.first { users ->
            users.any { it.id == generatedId && !it.isVerified && !it.isPending }
        }
        val updatedUser = updatedList.first { it.id == generatedId }
        assertFalse(updatedUser.isVerified)
        assertFalse(updatedUser.isPending)

        collectJob.cancel()
    }

    @Test
    fun testMyPropertyListings_and_SavedPropertyListings_flows() = runTest {
        val user = UserEntity(
            id = 1,
            uid = "user_me",
            fullName = "Parag Shah",
            phoneNumber = "9988776655",
            society = "Sylvan County",
            blockTower = "Block A",
            flatNumber = "101",
            isCurrent = true,
            isVerified = true
        )
        dao.insertUser(user)

        val listing1 = ListingEntity(
            id = 1,
            type = "PROPERTY",
            title = "My Property",
            description = "Spacious apartment",
            price = 30000.0,
            authorUid = "user_me",
            isBookmarked = true,
            isDraft = false
        )
        val listing2 = ListingEntity(
            id = 2,
            type = "PROPERTY",
            title = "Other's Property",
            description = "Villa",
            price = 50000.0,
            authorUid = "user_other",
            isBookmarked = true,
            isDraft = false
        )
        val listing3 = ListingEntity(
            id = 3,
            type = "PROPERTY",
            title = "Unsaved Property",
            description = "Studio",
            price = 15000.0,
            authorUid = "user_other",
            isBookmarked = false,
            isDraft = false
        )
        dao.insertListing(listing1)
        dao.insertListing(listing2)
        dao.insertListing(listing3)

        val myJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.myPropertyListings.collect {}
        }
        val savedJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.savedPropertyListings.collect {}
        }

        testDispatcher.scheduler.advanceUntilIdle()

        val myList = viewModel.myPropertyListings.first { it.isNotEmpty() }
        assertEquals(1, myList.size)
        assertEquals("user_me", myList[0].authorUid)

        val savedList = viewModel.savedPropertyListings.first { it.size >= 2 }
        assertEquals(2, savedList.size)
        assertTrue(savedList.all { it.isBookmarked && it.type == "PROPERTY" })

        myJob.cancel()
        savedJob.cancel()
    }

    @Test
    fun testExploredSocieties_capsAtThree() = runTest {
        val user = UserEntity(
            id = 1,
            uid = "user_explore",
            fullName = "Explorer",
            phoneNumber = "1234567890",
            society = "Home Society",
            blockTower = "Block A",
            flatNumber = "101",
            isCurrent = true,
            isVerified = true,
            exploredSocietyIds = emptyList()
        )
        dao.insertUser(user)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.exploredSocieties.collect {}
        }
        val userJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.currentUser.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.enterExploreMode("Society 1")
        viewModel.exploredSocieties.first { it.contains("Society 1") }

        viewModel.enterExploreMode("Society 2")
        viewModel.exploredSocieties.first { it.contains("Society 2") }

        viewModel.enterExploreMode("Society 3")
        viewModel.exploredSocieties.first { it.size == 3 }

        viewModel.enterExploreMode("Society 4")
        testDispatcher.scheduler.advanceUntilIdle()

        val explored = viewModel.exploredSocieties.first { it.size == 3 }
        assertEquals(3, explored.size)
        assertEquals(listOf("Society 1", "Society 2", "Society 3"), explored)

        collectJob.cancel()
        userJob.cancel()
    }

    @Test
    fun testExitExploreMode_restoresHomeSociety() = runTest {
        val user = UserEntity(
            id = 1,
            uid = "user_home",
            fullName = "Home Resident",
            phoneNumber = "9876543210",
            society = "Greenwood",
            blockTower = "Tower 1",
            flatNumber = "501",
            isCurrent = true,
            isVerified = true
        )
        dao.insertUser(user)

        val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.currentUser.collect {}
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Wait for currentUser to be populated in StateFlow
        viewModel.currentUser.first { it != null }

        // Enter explore mode
        viewModel.enterExploreMode("Silver Oak")
        val selected = viewModel.selectedSociety.first { it == "Silver Oak" }
        assertEquals("Silver Oak", selected)

        // Exit explore mode
        viewModel.exitExploreMode()
        val restored = viewModel.selectedSociety.first { it == "Greenwood" }
        assertEquals("Greenwood", restored)

        collectJob.cancel()
    }
}
