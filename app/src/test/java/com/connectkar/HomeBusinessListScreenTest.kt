package com.connectkar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.connectkar.data.local.HomeBusinessDetailsJson
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.MoshiHelper
import com.connectkar.data.local.UserEntity
import com.connectkar.ui.HomeBusinessListScreen
import com.connectkar.ui.SyncState
import com.connectkar.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeBusinessListScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    private val testUser = UserEntity(
        id = 1,
        uid = "user_1",
        fullName = "Test Resident",
        phoneNumber = "9876543210",
        society = "Sylvan County",
        blockTower = "Tower A",
        flatNumber = "101",
        isVerified = true
    )

    private val homeBusinessDetails = HomeBusinessDetailsJson(
        category = "Bakers",
        operatesFromFlat = "Tower B - 402",
        businessHours = "9 AM - 8 PM",
        priceRange = "₹150 - ₹1200"
    )

    private val testListings = listOf(
        ListingEntity(
            id = 10,
            type = "HOME_BUSINESS",
            title = "Sweet Treats Bakery",
            description = "Freshly baked artisan sourdough bread and custom cakes.",
            price = 450.0,
            society = "Sylvan County",
            category = "Bakers",
            detailsJson = MoshiHelper.toJson(homeBusinessDetails),
            authorName = "Pooja Verma",
            authorPhone = "9876543210"
        ),
        ListingEntity(
            id = 11,
            type = "HOME_BUSINESS",
            title = "MindCraft Tutoring",
            description = "Personalized tutoring for grades 6-10 CBSE.",
            price = 1200.0,
            society = "Sylvan County",
            category = "Tutors",
            detailsJson = MoshiHelper.toJson(homeBusinessDetails),
            authorName = "Arjun Rao",
            authorPhone = "9123456780"
        )
    )

    @Test
    fun testHomeBusinessListScreen_displaysListingsAndHeaders() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeBusinessListScreen(
                    listings = testListings,
                    currentUser = testUser,
                    selectedSociety = "Sylvan County",
                    syncState = SyncState.Idle,
                    hasPostedListings = true,
                    onBack = {},
                    onLikeListing = {},
                    onBookmarkListing = {},
                    onCreateListingClicked = {},
                    onRetrySync = {}
                )
            }
        }

        // Verify title exists
        composeTestRule.onNodeWithTag("screen_title_home_businesses").assertExists()
        // Verify search input exists
        composeTestRule.onNodeWithTag("home_biz_search_input").assertExists()
        // Verify category chips row exists
        composeTestRule.onNodeWithTag("home_biz_category_chips").assertExists()
        // Verify listing cards exist
        composeTestRule.onNodeWithTag("listing_card_10").assertExists()
    }

    @Test
    fun testHomeBusinessListScreen_fabPulsesWhenVerifiedAndNoListings() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeBusinessListScreen(
                    listings = testListings,
                    currentUser = testUser.copy(isVerified = true),
                    selectedSociety = "Sylvan County",
                    syncState = SyncState.Idle,
                    hasPostedListings = false,
                    onBack = {},
                    onLikeListing = {},
                    onBookmarkListing = {},
                    onCreateListingClicked = {},
                    onRetrySync = {}
                )
            }
        }

        // Both FAB and pulse halo must exist
        composeTestRule.onNodeWithTag("home_biz_fab").assertExists()
        composeTestRule.onNodeWithTag("home_biz_pulse_halo").assertExists()
    }

    @Test
    fun testHomeBusinessListScreen_fabDoesNotPulseWhenUserHasPostedListings() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeBusinessListScreen(
                    listings = testListings,
                    currentUser = testUser.copy(isVerified = true),
                    selectedSociety = "Sylvan County",
                    syncState = SyncState.Idle,
                    hasPostedListings = true,
                    onBack = {},
                    onLikeListing = {},
                    onBookmarkListing = {},
                    onCreateListingClicked = {},
                    onRetrySync = {}
                )
            }
        }

        // FAB must exist, but halo must NOT exist
        composeTestRule.onNodeWithTag("home_biz_fab").assertExists()
        composeTestRule.onNodeWithTag("home_biz_pulse_halo").assertDoesNotExist()
    }

    @Test
    fun testHomeBusinessListScreen_fabHiddenWhenUserNotVerified() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeBusinessListScreen(
                    listings = testListings,
                    currentUser = testUser.copy(isVerified = false),
                    selectedSociety = "Sylvan County",
                    syncState = SyncState.Idle,
                    hasPostedListings = false,
                    onBack = {},
                    onLikeListing = {},
                    onBookmarkListing = {},
                    onCreateListingClicked = {},
                    onRetrySync = {}
                )
            }
        }

        // FAB must not exist when unverified
        composeTestRule.onNodeWithTag("home_biz_fab").assertDoesNotExist()
        composeTestRule.onNodeWithTag("home_biz_pulse_halo").assertDoesNotExist()
    }
}
