package com.connectkar.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.connectkar.auth.AuthViewModel
import com.connectkar.ui.theme.ConnectKarTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FirebaseAuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        val testDispatcher = UnconfinedTestDispatcher()
        viewModel = AuthViewModel(dispatcher = testDispatcher)
        composeTestRule.setContent {
            ConnectKarTheme {
                FirebaseAuthScreen(viewModel = viewModel)
            }
        }
    }

    @Test
    fun loginScreen_rendersInitialComponents() {
        composeTestRule.onNodeWithTag("firebase_auth_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("app_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("email_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("domain_chips_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("password_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_in_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun unauthorizedDomain_displaysErrorMessage() {
        composeTestRule.onNodeWithTag("email_input").performTextInput("intruder@external-domain.com")
        composeTestRule.onNodeWithTag("password_input").performTextInput("password123")
        composeTestRule.onNodeWithTag("sign_in_button").performScrollTo().performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("email_error_text").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun domainPillClick_updatesEmailWithDomain() {
        composeTestRule.onNodeWithTag("domain_pill_connectkar.com").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("resident@connectkar.com").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun authorizedDomain_signsInSuccessfully() {
        composeTestRule.onNodeWithTag("email_input").performTextInput("alice.smith@resident.community")
        composeTestRule.onNodeWithTag("password_input").performTextInput("ValidPass123")
        composeTestRule.onNodeWithTag("sign_in_button").performScrollTo().performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("verified_resident_dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("resident_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_out_button").performScrollTo().assertIsDisplayed()

        // Sign out returns to login screen
        composeTestRule.onNodeWithTag("sign_out_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("sign_in_button").performScrollTo().assertIsDisplayed()
    }
}
