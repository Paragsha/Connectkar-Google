package com.connectkar

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.connectkar.data.local.AppDao
import com.connectkar.data.local.HomeBusinessDetailsJson
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.MoshiHelper
import com.connectkar.data.local.UserEntity
import com.connectkar.data.repository.TownshipRepository
import com.connectkar.ui.CreateListingScreen
import com.connectkar.ui.CreateListingViewModel
import com.connectkar.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeBusinessCreateFlowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val verifiedUser = UserEntity(
        id = 1,
        uid = "user_biz_1",
        fullName = "Anita Sharma",
        phoneNumber = "9876543210",
        society = "Sylvan County",
        blockTower = "Tower B",
        flatNumber = "402",
        isVerified = true
    )

    private val unverifiedUser = UserEntity(
        id = 2,
        uid = "user_biz_2",
        fullName = "Rohan Verma",
        phoneNumber = "9123456780",
        society = "Sylvan County",
        blockTower = "Tower C",
        flatNumber = "105",
        isVerified = false
    )

    private fun setupViewModel(): Pair<CreateListingViewModel, AppDao> {
        val app = ApplicationProvider.getApplicationContext<Application>() as ConnectKarApplication
        val vm = CreateListingViewModel(app)
        vm.setActiveType("HOME_BUSINESS")
        return Pair(vm, app.database.appDao())
    }

    @Test
    fun testHomeBusinessCreateForm_rendersAllComponents() {
        val (viewModel, _) = setupViewModel()

        composeTestRule.setContent {
            MyApplicationTheme {
                CreateListingScreen(
                    initialType = "HOME_BUSINESS",
                    currentUser = verifiedUser,
                    viewModel = viewModel,
                    onBack = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Top bar title
        composeTestRule.onNodeWithText("List Your Business").assertIsDisplayed()

        // Banner
        composeTestRule.onNodeWithText("Grow inside your community").assertIsDisplayed()

        // Form elements exist in composition
        composeTestRule.onNodeWithTag("home_business_title_input").assertExists()
        composeTestRule.onNodeWithTag("home_business_category_Baking & Food").assertExists()
        composeTestRule.onNodeWithTag("home_business_category_Tutoring").assertExists()
        composeTestRule.onNodeWithText("Anita Sharma").assertExists()
        composeTestRule.onNodeWithText("Verified Resident").assertExists()
        composeTestRule.onNodeWithTag("home_business_edit_contact_button").assertExists()
        composeTestRule.onNodeWithTag("home_business_same_as_contact_switch").assertExists()
        composeTestRule.onNodeWithTag("home_business_description_input").assertExists()
        composeTestRule.onNodeWithTag("home_business_price_range_input").assertExists()
        composeTestRule.onNodeWithTag("home_business_photo_picker_empty").assertExists()
        composeTestRule.onNodeWithTag("home_business_instagram_input").assertExists()
        composeTestRule.onNodeWithTag("home_business_recurring_switch").assertExists()
        composeTestRule.onNodeWithTag("home_business_submit_button").assertExists()
    }

    @Test
    fun testHomeBusinessCreateForm_validationFailsOnEmptyFields() {
        val (viewModel, _) = setupViewModel()

        composeTestRule.setContent {
            MyApplicationTheme {
                CreateListingScreen(
                    initialType = "HOME_BUSINESS",
                    currentUser = verifiedUser,
                    viewModel = viewModel,
                    onBack = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Tap submit without entering title or category
        composeTestRule.onNodeWithTag("home_business_submit_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Should show validation error for title and category
        composeTestRule.onAllNodesWithText("Please enter a business title.").onFirst().assertExists()
        composeTestRule.onAllNodesWithText("Please select a category.").onFirst().assertExists()
        composeTestRule.onAllNodesWithText("Please enter a description.").onFirst().assertExists()
    }

    @Test
    fun testHomeBusinessCreateForm_unverifiedUserBlockedFromPosting() {
        val (viewModel, _) = setupViewModel()
        var successCalled = false

        composeTestRule.setContent {
            MyApplicationTheme {
                CreateListingScreen(
                    initialType = "HOME_BUSINESS",
                    currentUser = unverifiedUser,
                    viewModel = viewModel,
                    onBack = { successCalled = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Enter valid fields
        composeTestRule.onNodeWithTag("home_business_title_input").performScrollTo().performTextInput("Rohan's Tuitions")
        composeTestRule.onNodeWithTag("home_business_category_Tutoring").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("home_business_description_input")
            .performScrollTo().performTextInput("Mathematics coaching for grades 8-10 CBSE and ICSE syllabus.")

        // Click submit
        composeTestRule.onNodeWithTag("home_business_submit_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // Should NOT have succeeded
        assertFalse(successCalled)
    }

    @Test
    fun testHomeBusinessCreateForm_successfulPublishPersistsEntityAndDetailsJson() {
        val (viewModel, appDao) = setupViewModel()
        var successBackCalled = false

        composeTestRule.setContent {
            MyApplicationTheme {
                CreateListingScreen(
                    initialType = "HOME_BUSINESS",
                    currentUser = verifiedUser,
                    viewModel = viewModel,
                    onBack = { successBackCalled = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Fill form
        composeTestRule.onNodeWithTag("home_business_title_input").performScrollTo().performTextInput("Artisan Breads & Pies")
        composeTestRule.onNodeWithTag("home_business_category_Baking & Food").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("home_business_description_input")
            .performScrollTo().performTextInput("Freshly baked sourdough and organic fruit pies made with love.")
        composeTestRule.onNodeWithTag("home_business_price_range_input").performScrollTo().performTextInput("₹250 - ₹1,200")
        composeTestRule.onNodeWithTag("home_business_instagram_input").performScrollTo().performTextInput("@artisan_pies")

        // Click Post Business
        composeTestRule.onNodeWithTag("home_business_submit_button").performScrollTo().performClick()
        
        var retries = 0
        while (!successBackCalled && retries < 50) {
            composeTestRule.waitForIdle()
            Thread.sleep(50)
            retries++
        }

        // Verify navigation back triggered
        assertTrue(successBackCalled)

        // Verify entity persisted in Room database
        runBlocking {
            val listings = appDao.getListingsByTypeAndSociety("HOME_BUSINESS", "Sylvan County").firstOrNull() ?: emptyList()
            val biz = listings.find { it.title == "Artisan Breads & Pies" }
            assertNotNull("Listing should be saved in DB", biz)
            assertEquals("HOME_BUSINESS", biz?.type)
            assertEquals("Baking & Food", biz?.category)
            assertEquals("Anita Sharma", biz?.authorName)
            assertEquals("9876543210", biz?.authorPhone)
            assertEquals(0.0, biz?.price ?: -1.0, 0.001)

            // Check detailsJson
            assertNotNull(biz?.detailsJson)
            val details = MoshiHelper.fromJson<HomeBusinessDetailsJson>(biz!!.detailsJson!!)
            assertNotNull("detailsJson should deserialize correctly", details)
            assertEquals("Baking & Food", details?.category)
            assertEquals("Tower B - 402", details?.operatesFromFlat)
            assertEquals("₹250 - ₹1,200", details?.priceRange)
            assertTrue(details?.instagramHandle == "@artisan_pies" || details?.instagramHandle == "artisan_pies")
            assertEquals(true, details?.isRecurring)
            assertEquals("9876543210", details?.whatsappNumber)
        }
    }
}
