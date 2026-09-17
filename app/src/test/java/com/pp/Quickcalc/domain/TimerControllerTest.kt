package com.pp.Quickcalc.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerControllerTest {

    @Test
    fun start_initializesTimeAndCalculatesProgressFraction() = runTest {
        val timer = TimerController(this, tickMs = 50L)
        var expired = false

        timer.start(1000L) { expired = true }

        assertEquals(1.0f, timer.progressFraction(), 0.01f)
        assertEquals(1000L, timer.timeLeftMs.value)

        testScheduler.advanceTimeBy(500L)
        testScheduler.runCurrent()
        assertTrue("Progress fraction should be positive", timer.progressFraction() > 0f)

        testScheduler.advanceTimeBy(600L)
        testScheduler.runCurrent()
        assertEquals(0.0f, timer.progressFraction(), 0.01f)
        assertTrue(expired)
    }

    @Test
    fun addTime_increasesTimeCappedAtMax() = runTest {
        val timer = TimerController(this, tickMs = 50L)
        timer.start(1000L) {}

        testScheduler.advanceTimeBy(500L)
        testScheduler.runCurrent()
        val initialLeft = timer.timeLeftMs.value

        timer.addTime(300L)
        assertEquals((initialLeft + 300L).coerceAtMost(1000L), timer.timeLeftMs.value)

        timer.addTime(500L)
        assertEquals(1000L, timer.timeLeftMs.value)
    }

    @Test
    fun stop_cancelsTimerJob() = runTest {
        val timer = TimerController(this, tickMs = 50L)
        var expired = false

        timer.start(1000L) { expired = true }
        testScheduler.advanceTimeBy(200L)
        testScheduler.runCurrent()
        timer.stop()

        testScheduler.advanceTimeBy(1000L)
        testScheduler.runCurrent()
        assertFalse(expired)
    }
}
