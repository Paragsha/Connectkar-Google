package com.connectkar

import android.app.Application
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import com.connectkar.data.local.UserEntity
import com.connectkar.data.repository.TownshipRepository
import com.connectkar.ui.CreateHubScreen
import com.connectkar.ui.CreateHubViewModel
import com.connectkar.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CreateHubScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUser = UserEntity(
        id = 1,
        uid = "user_hub_test",
        fullName = "Aarav Patel",
        phoneNumber = "9876543210",
        society = "Aqualily",
        blockTower = "Tower B",
        flatNumber = "402",
        isVerified = true
    )

    @Test
    fun testCreateHubScreen_rendersHeaderSectionsAndCards() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val appInstance = app as ConnectKarApplication
        val repository = TownshipRepository(appInstance.database.appDao(), app)
        val viewModel = CreateHubViewModel(repository, app)

        var selectedCategory: String? = null
        var bottomNavSelected: String? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                CreateHubScreen(
                    currentUser = testUser,
                    viewModel = viewModel,
                    onBack = {},
                    onNavigateToCreateFlow = { selectedCategory = it },
                    onBottomNavClick = { bottomNavSelected = it }
                )
            }
        }

        // Header controls
        composeTestRule.onNodeWithTag("create_hub_close_button").assertExists()
        composeTestRule.onNodeWithTag("create_hub_app_title").assertExists()
        composeTestRule.onNodeWithTag("create_hub_draft_label").assertExists()

        // Suggested badge and meal card
        composeTestRule.onNodeWithTag("create_hub_suggested_badge", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("category_meal_card").assertExists()

        // Section headers and Category cards
        composeTestRule.onNodeWithTag("create_hub_grid").performScrollToNode(hasTestTag("section_header_buy_and_sell"))
        composeTestRule.onNodeWithTag("section_header_buy_and_sell").assertExists()
        composeTestRule.onNodeWithTag("category_marketplace_card").assertExists()
        composeTestRule.onNodeWithTag("category_property_hero_card").assertExists()

        composeTestRule.onNodeWithTag("create_hub_grid").performScrollToNode(hasTestTag("section_header_community"))
        composeTestRule.onNodeWithTag("section_header_community").assertExists()
        composeTestRule.onNodeWithTag("category_community_post_card").assertExists()
        composeTestRule.onNodeWithTag("category_event_card").assertExists()

        composeTestRule.onNodeWithTag("create_hub_grid").performScrollToNode(hasTestTag("section_header_services"))
        composeTestRule.onNodeWithTag("section_header_services").assertExists()
        composeTestRule.onNodeWithTag("category_service_card").assertExists()
        composeTestRule.onNodeWithTag("category_carpool_card").assertExists()

        composeTestRule.onNodeWithTag("create_hub_grid").performScrollToNode(hasTestTag("verified_community_banner"))
        composeTestRule.onNodeWithTag("verified_community_banner").assertExists()

        // Click COMMUNITY_POST card
        composeTestRule.onNodeWithTag("create_hub_grid").performScrollToNode(hasTestTag("category_community_post_card"))
        composeTestRule.onNodeWithTag("category_community_post_card").performClick()
        assertEquals("COMMUNITY_POST", selectedCategory)
    }

    @Test
    fun testCreateHubScreen_searchFiltersCategories() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val appInstance = app as ConnectKarApplication
        val repository = TownshipRepository(appInstance.database.appDao(), app)
        val viewModel = CreateHubViewModel(repository, app)

        composeTestRule.setContent {
            MyApplicationTheme {
                CreateHubScreen(
                    currentUser = testUser,
                    viewModel = viewModel,
                    onBack = {},
                    onNavigateToCreateFlow = {}
                )
            }
        }

        // Search for "Carpool"
        composeTestRule.onNodeWithTag("create_hub_search_input").performTextInput("Carpool")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("category_carpool_card").assertExists()
        composeTestRule.onNodeWithTag("category_marketplace_card").assertDoesNotExist()
    }
}
