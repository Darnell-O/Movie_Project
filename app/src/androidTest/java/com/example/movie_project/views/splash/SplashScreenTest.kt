package com.example.movie_project.views.splash

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SplashScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun splashScreen_displaysLogo() {
        composeTestRule.setContent {
            SplashScreen(onTimeout = {}, splashDurationMs = 5_000L)
        }
        composeTestRule.onNodeWithContentDescription("Movie Magic logo")
            .assertIsDisplayed()
    }

    @Test
    fun splashScreen_callsOnTimeoutAfterDelay() {
        var called = false
        composeTestRule.setContent {
            SplashScreen(onTimeout = { called = true }, splashDurationMs = 100L)
        }
        composeTestRule.waitUntil(timeoutMillis = 2_000L) { called }
        assertTrue("onTimeout should have been called after the delay", called)
    }

    @Test
    fun splashScreen_doesNotCallOnTimeoutImmediately() {
        var called = false
        composeTestRule.setContent {
            SplashScreen(onTimeout = { called = true }, splashDurationMs = 5_000L)
        }
        // Right after composition, the callback should not have fired yet
        assertFalse("onTimeout should not be called immediately", called)
    }
}