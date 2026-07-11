package com.example

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.ui.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OnboardingScreenTest {

    @get:Rule val composeTestRule = createComposeRule()

    @org.junit.Before
    fun setup() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        try {
            val config = androidx.work.Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor())
                .build()
            androidx.work.WorkManager.initialize(context, config)
        } catch (e: Exception) {
            // Already initialized or exception ignored
        }
    }

    @Test
    fun testOnboardingForm_validationAndSimulatedOTPFlow() {
        var registerResult: RegisteredData? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                OnboardingScreen(
                    onRegisterSuccess = { fullName, phone, society, block, flat, avatar, floor, residentType, moveInDate, proofDocumentUri ->
                        registerResult = RegisteredData(
                            fullName, phone, society, block, flat, avatar, floor, residentType, moveInDate, proofDocumentUri
                        )
                    }
                )
            }
        }

        // --- STEP 1: Phone Entry & Verification ---
        // 1. Initially, the "Send Verification OTP" button should be disabled because the phone entry is incomplete
        composeTestRule.onNodeWithTag("submit_onboarding_button").assertIsNotEnabled()

        // 2. Input valid name and phone
        composeTestRule.onNodeWithTag("onboarding_fullname").performTextInput("Rahul Kumar")
        composeTestRule.onNodeWithTag("onboarding_phone").performTextInput("9876543210")

        // 3. Now, the "Send Verification OTP" button should be enabled
        composeTestRule.onNodeWithTag("submit_onboarding_button").assertIsEnabled()

        // 4. Click the button to send OTP (this launches simulated OTP in debug build)
        composeTestRule.onNodeWithTag("submit_onboarding_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 5. The OTP popup dialog should appear. We expect the OTP input field to exist.
        composeTestRule.onNodeWithTag("otp_input_field").assertExists()

        // 6. Test edge case: Enter incorrect OTP code and verify error feedback
        composeTestRule.onNodeWithTag("otp_input_field").performTextInput("654321")
        composeTestRule.onNodeWithTag("verify_otp_confirm_button").performClick()
        composeTestRule.waitForIdle()

        // Check that registration did not succeed yet
        assertNull(registerResult)

        // Enter correct OTP to verify phone number and transition to Step 2
        composeTestRule.onNodeWithTag("otp_input_field").performTextReplacement("123456")
        composeTestRule.onNodeWithTag("verify_otp_confirm_button").performClick()
        composeTestRule.waitForIdle()

        // --- STEP 2: Residence Details ---
        // Verify Residence input fields are now visible
        composeTestRule.onNodeWithTag("onboarding_block").assertExists()
        composeTestRule.onNodeWithTag("onboarding_flat").assertExists()
        composeTestRule.onNodeWithTag("onboarding_floor").assertExists()
        composeTestRule.onNodeWithTag("onboarding_move_in_date").assertExists()
        composeTestRule.onNodeWithTag("onboarding_proof_doc").assertExists()

        // Input residence fields
        composeTestRule.onNodeWithTag("onboarding_block").performTextInput("Block B")
        composeTestRule.onNodeWithTag("onboarding_flat").performTextInput("502")
        composeTestRule.onNodeWithTag("onboarding_floor").performTextInput("5")
        composeTestRule.onNodeWithTag("onboarding_move_in_date").performTextInput("2026-07-05")
        
        // Tap proof document to simulate upload selection
        composeTestRule.onNodeWithTag("onboarding_proof_doc").performClick()
        composeTestRule.waitForIdle()

        // Submit Step 2 residence form to transition to Step 3
        composeTestRule.onNodeWithTag("register_residence_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // --- STEP 3: Success ---
        // Click the finish button to call onRegisterSuccess
        composeTestRule.onNodeWithTag("finish_onboarding_button").performClick()
        composeTestRule.waitForIdle()

        // 7. Verify that registration succeeded and correct arguments are passed back
        assertNotNull(registerResult)
        assertEquals("Rahul Kumar", registerResult?.fullName)
        assertEquals("9876543210", registerResult?.phone)
        assertEquals("Sylvan County", registerResult?.society)
        assertEquals("Block B", registerResult?.block)
        assertEquals("502", registerResult?.flat)
        assertEquals("5", registerResult?.floor)
        assertEquals("OWNER", registerResult?.residentType)
        assertEquals("2026-07-05", registerResult?.moveInDate)
        assertEquals("simulated_proof_of_residence.pdf", registerResult?.proofDocumentUri)
    }

    private data class RegisteredData(
        val fullName: String,
        val phone: String,
        val society: String,
        val block: String,
        val flat: String,
        val avatar: Int,
        val floor: String,
        val residentType: String,
        val moveInDate: String,
        val proofDocumentUri: String
    )
}
