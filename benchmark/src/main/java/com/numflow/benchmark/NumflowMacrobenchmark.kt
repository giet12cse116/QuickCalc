package com.numflow.benchmark

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NumflowMacrobenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    /**
     * Measures Cold Startup time and time to navigate from Home screen to Game screen.
     */
    @Test
    fun startupAndPlayNowNavigationMetric() = benchmarkRule.measureRepeated(
        packageName = "com.numflow.game",
        metrics = listOf(StartupTimingMetric(), FrameTimingMetric()),
        compilationMode = CompilationMode.DEFAULT,
        iterations = 5,
        startupMode = StartupMode.COLD
    ) {
        pressHome()
        startActivityAndWait()

        // Locate and click PLAY NOW button
        val playNowButton = device.wait(Until.findObject(By.text("PLAY NOW ▶")), 5000)
        playNowButton?.click()

        // Wait until Game screen expression appears
        device.wait(Until.hasObject(By.res("game_screen_number_display")), 5000)
    }

    /**
     * Measures FrameTimingMetric (Jank frame percentage & frame duration percentiles)
     * during continuous scrolling on the Level Select screen across 200 items.
     */
    @Test
    fun levelSelectScrollFrameTimingMetric() = benchmarkRule.measureRepeated(
        packageName = "com.numflow.game",
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.DEFAULT,
        iterations = 5,
        startupMode = StartupMode.WARM
    ) {
        pressHome()
        startActivityAndWait()

        // Navigate to CHOOSE LEVEL / Level Select screen
        val chooseLevelButton = device.wait(Until.findObject(By.text("CHOOSE LEVEL")), 5000)
        chooseLevelButton?.click()

        val gridList = device.wait(Until.findObject(By.scrollable(true)), 5000)
        gridList?.setAsHorizontalList() // or vertical fling

        // Perform continuous programmatic flings to measure frame rendering performance
        repeat(5) {
            gridList?.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }
}
