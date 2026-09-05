package com.example

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.UserEntity
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainActivityComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testMainActivityRendersWhenLoggedOut() {
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().printToLog("MainActivityComposeTest")
    }

    @Test
    fun testMainActivityRendersWhenLoggedIn() {
        val app = ApplicationProvider.getApplicationContext<ConnectKarApplication>()
        runBlocking {
            app.database.appDao().insertUser(
                UserEntity(
                    uid = "test-uid-123",
                    fullName = "Parag Shah",
                    phoneNumber = "9876543210",
                    society = "Sylvan County",
                    blockTower = "A",
                    flatNumber = "101",
                    isVerified = true,
                    isPending = false,
                    isCurrent = true
                )
            )
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().printToLog("MainActivityComposeTest")
    }
}
