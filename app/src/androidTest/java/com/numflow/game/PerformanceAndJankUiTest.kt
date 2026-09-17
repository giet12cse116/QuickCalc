package com.pp.Quickcalc

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.fetchSemanticsNodes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PerformanceAndJankUiTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    /**
     * TEST 1: Asserts that tapping "PLAY NOW" transitions directly to the Game screen
     * within a strict 300ms latency budget.
     */
    @Test
    fun testPlayNowNavigationTimeBudget() {
        val startTimeMs = System.currentTimeMillis()

        // Tap PLAY NOW button on Home screen
        composeTestRule
            .onNodeWithTag("play_now_button")
            .assertIsDisplayed()
            .performClick()

        // Assert Game screen key composable becomes visible within 300ms time budget
        composeTestRule.waitUntil(timeoutMillis = 300) {
            composeTestRule
                .onAllNodesWithTag("game_screen_number_display")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        val elapsed = System.currentTimeMillis() - startTimeMs
        assertTrue("Navigation latency exceeded 300ms budget: ${elapsed}ms", elapsed <= 300)
    }

    /**
     * TEST 2: Frame-by-frame clock stepping after tapping PLAY NOW.
     * Uses mainClock.autoAdvance = false to measure exact frame progression.
     */
    @Test
    fun testPlayNowFrameByFrameClockStepping() {
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule
            .onNodeWithTag("play_now_button")
            .assertIsDisplayed()
            .performClick()

        // Advance 1 frame (16ms)
        composeTestRule.mainClock.advanceTimeBy(16)
        composeTestRule.mainClock.advanceTimeByFrame()

        // Advance further 10 frames (160ms total)
        repeat(10) {
            composeTestRule.mainClock.advanceTimeBy(16)
        }

        // Resume autoAdvance to complete assertions cleanly
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule
            .onNodeWithTag("game_screen_number_display")
            .assertIsDisplayed()
    }
}
