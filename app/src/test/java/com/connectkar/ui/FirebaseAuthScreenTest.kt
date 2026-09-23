package com.connectkar.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.connectkar.auth.AuthViewModel
import com.connectkar.data.repository.FirebaseAuthRepository
import com.connectkar.ui.theme.ConnectKarTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
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
        FirebaseAuthRepository.resetInstanceForTesting()
        val testDispatcher = UnconfinedTestDispatcher()
        val authRepository = FirebaseAuthRepository(dispatcher = testDispatcher)
        viewModel = AuthViewModel(authRepository = authRepository, dispatcher = testDispatcher)
        composeTestRule.setContent {
            ConnectKarTheme {
                FirebaseAuthScreen(viewModel = viewModel)
            }
        }
    }

    @After
    fun tearDown() {
        FirebaseAuthRepository.resetInstanceForTesting()
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

    @Test
    fun passwordStrengthMeter_displaysWhenTypingPassword() {
        composeTestRule.onNodeWithTag("password_input").performTextInput("Weak1")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("password_strength_bar").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun forgotPassword_opensDialogAndAllowsSubmittingRecovery() {
        composeTestRule.onNodeWithTag("forgot_password_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("reset_password_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reset_password_input").performTextInput("resident@connectkar.com")
        composeTestRule.onNodeWithTag("send_reset_button").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun registrationScreen_rendersAllFormComponents() {
        composeTestRule.onNodeWithTag("auth_tab_register").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("reg_title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_name_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_unit_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_email_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_domain_chips_row").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_password_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_confirm_password_input").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_terms_checkbox").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("register_button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun registrationForm_validationErrors_whenFieldsAreEmptyOrInvalid() {
        composeTestRule.onNodeWithTag("switch_to_register_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 1. Click register with empty form
        composeTestRule.onNodeWithTag("register_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("reg_name_error_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_unit_error_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_email_error_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_password_error_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_confirm_password_error_text").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("reg_terms_error_text").performScrollTo().assertIsDisplayed()

        // 2. Test password mismatch
        composeTestRule.onNodeWithTag("reg_name_input").performScrollTo().performTextInput("John Resident")
        composeTestRule.onNodeWithTag("reg_unit_input").performScrollTo().performTextInput("Tower A - 501")
        composeTestRule.onNodeWithTag("reg_email_input").performScrollTo().performTextInput("john@resident.community")
        composeTestRule.onNodeWithTag("reg_password_input").performScrollTo().performTextInput("Password123")
        composeTestRule.onNodeWithTag("reg_confirm_password_input").performScrollTo().performTextInput("Mismatch456")
        composeTestRule.onNodeWithTag("reg_terms_checkbox").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("register_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("reg_confirm_password_error_text").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun registrationForm_successfulAccountCreation_linksToAuthRepository() {
        composeTestRule.onNodeWithTag("auth_tab_register").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("reg_name_input").performScrollTo().performTextInput("Eleanor Vance")
        composeTestRule.onNodeWithTag("reg_unit_input").performScrollTo().performTextInput("Tower C - 104")
        composeTestRule.onNodeWithTag("reg_email_input").performScrollTo().performTextInput("eleanor@connectkar.com")
        composeTestRule.onNodeWithTag("reg_password_input").performScrollTo().performTextInput("StrongPass!99")
        composeTestRule.onNodeWithTag("reg_confirm_password_input").performScrollTo().performTextInput("StrongPass!99")
        composeTestRule.onNodeWithTag("reg_terms_checkbox").performScrollTo().performClick()

        composeTestRule.onNodeWithTag("register_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("verified_resident_dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithTag("resident_display_name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Eleanor Vance").assertIsDisplayed()
    }
}
