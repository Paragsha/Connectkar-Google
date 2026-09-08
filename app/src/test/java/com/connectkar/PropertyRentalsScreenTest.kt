package com.connectkar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.UserEntity
import com.connectkar.ui.PropertyRentalsScreen
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
class PropertyRentalsScreenTest {

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

    private val testListings = listOf(
        ListingEntity(
            id = 1,
            type = "PROPERTY",
            title = "Cozy 1 BHK Flat",
            description = "Affordable and cozy 1 BHK apartment.",
            price = 14000.0,
            society = "Sylvan County",
            category = "1 BHK Rent"
        ),
        ListingEntity(
            id = 2,
            type = "PROPERTY",
            title = "Modern 2 BHK Apartment",
            description = "Spacious modern apartment with balcony.",
            price = 28000.0,
            society = "Sylvan County",
            category = "2 BHK Rent"
        ),
        ListingEntity(
            id = 3,
            type = "PROPERTY",
            title = "Luxury 3 BHK Penthouse",
            description = "Stunning luxury penthouse with skyline view.",
            price = 65000.0,
            society = "Sylvan County",
            category = "3 BHK Rent"
        )
    )

    @Test
    fun testPropertyRentalsScreen_showsListingsAndFilterBottomSheet() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PropertyRentalsScreen(
                    currentUser = testUser,
                    listings = testListings,
                    selectedSociety = "Sylvan County",
                    syncState = SyncState.Idle,
                    onBack = {},
                    onLikeListing = {},
                    onBookmarkListing = {},
                    onCreateListingClicked = {},
                    onRetrySync = {}
                )
            }
        }

        // Verify title and listings exist initially
        composeTestRule.onNodeWithTag("screen_title_rentals").assertExists()
        composeTestRule.onNodeWithTag("property_card_1").assertExists()

        // Open Filter Bottom Sheet
        composeTestRule.onNodeWithTag("property_filter_button").performClick()
        composeTestRule.waitForIdle()

        // Verify RangeSlider exists in bottom sheet
        composeTestRule.onNodeWithTag("price_range_slider").assertExists()
        composeTestRule.onNodeWithTag("apply_filters_button").assertExists()
        composeTestRule.onNodeWithTag("reset_filters_button").assertExists()
    }
}
