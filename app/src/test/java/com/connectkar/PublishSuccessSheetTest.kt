package com.connectkar

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.connectkar.data.local.ListingEntity
import com.connectkar.data.local.MoshiHelper
import com.connectkar.data.local.HomeBusinessDetailsJson
import com.connectkar.ui.PublishSuccessSheet
import com.connectkar.ui.theme.MyApplicationTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PublishSuccessSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleListing = ListingEntity(
        id = 42,
        firestoreId = "firestore_doc_123",
        type = "HOME_BUSINESS",
        title = "Priya's Home Kitchen",
        description = "Delicious homemade thalis and parathas prepared with fresh organic ingredients.",
        price = 0.0,
        contact = "9876543210",
        society = "Sylvan County",
        authorName = "Priya Patel",
        authorFlat = "Tower C - 302",
        authorPhone = "9876543210",
        authorUid = "user_priya_1",
        category = "Baking & Food",
        detailsJson = MoshiHelper.toJson(
            HomeBusinessDetailsJson(
                category = "Baking & Food",
                operatesFromFlat = "Tower C - 302",
                businessHours = "10 AM - 8 PM",
                priceRange = "₹150 - ₹500",
                whatsappNumber = "9876543210",
                instagramHandle = "@priyaskitchen",
                isRecurring = true
            )
        )
    )

    @Test
    fun testPublishSuccessSheet_displaysAllCoreElements() {
        var viewListingClicked = false
        var dismissClicked = false

        composeTestRule.setContent {
            MyApplicationTheme {
                PublishSuccessSheet(
                    listing = sampleListing,
                    isConfirmedLive = true,
                    onViewListing = { viewListingClicked = true },
                    onDismiss = { dismissClicked = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        // 1. Verify Sheet exists
        composeTestRule.onNodeWithTag("publish_success_sheet").assertExists()

        // 2. Verify Title and close button
        composeTestRule.onNodeWithTag("publish_success_title").assertExists()
        composeTestRule.onNodeWithTag("publish_success_close_button").assertExists()

        // 3. Verify Status Badge shows LIVE
        composeTestRule.onNodeWithTag("publish_success_status_badge").assertExists()
        composeTestRule.onNodeWithText("LIVE").assertExists()

        // 4. Verify Preview Card with details
        composeTestRule.onNodeWithTag("publish_success_preview_card").assertExists()
        composeTestRule.onNodeWithText("Priya's Home Kitchen").assertExists()
        composeTestRule.onNodeWithText("Baking & Food").assertExists()
        composeTestRule.onNodeWithText("Tower C - 302 • Sylvan County").assertExists()

        // 5. Verify Sharing options
        composeTestRule.onNodeWithTag("publish_success_share_whatsapp").assertExists()
        composeTestRule.onNodeWithTag("publish_success_share_society_buzz").assertExists()
        composeTestRule.onNodeWithText("Coming soon").assertExists() // D2
        composeTestRule.onNodeWithTag("publish_success_share_copy_link").assertExists()

        // 6. Verify Action Buttons
        composeTestRule.onNodeWithTag("publish_success_view_listing_button").assertExists()
        composeTestRule.onNodeWithTag("publish_success_done_button").assertExists()

        // 7. Test View Listing button click
        composeTestRule.onNodeWithTag("publish_success_view_listing_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue("onViewListing should have been invoked", viewListingClicked)

        // 8. Test Done button click
        composeTestRule.onNodeWithTag("publish_success_done_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertTrue("onDismiss should have been invoked", dismissClicked)
    }

    @Test
    fun testPublishSuccessSheet_unconfirmedLive_showsSavedSyncingBadge() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PublishSuccessSheet(
                    listing = sampleListing,
                    isConfirmedLive = false,
                    onViewListing = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // D1: Verify badge shows "SAVED • SYNCING TO CLOUD"
        composeTestRule.onNodeWithText("SAVED • SYNCING TO CLOUD").assertExists()
    }
}
