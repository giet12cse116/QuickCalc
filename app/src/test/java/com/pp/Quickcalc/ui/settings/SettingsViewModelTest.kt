package com.pp.Quickcalc.ui.settings

import com.pp.Quickcalc.data.HighScoreRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialPrefs_hasDefaultValues() = runTest {
        val repo = HighScoreRepository()
        val vm = SettingsViewModel(repo)

        val prefs = vm.prefs.value
        assertTrue(prefs.soundEnabled)
        assertTrue(prefs.vibrationEnabled)
        assertFalse(prefs.darkTheme)
    }

    @Test
    fun toggleSound_updatesState() = runTest {
        val repo = HighScoreRepository()
        val vm = SettingsViewModel(repo)

        vm.toggleSound(false)
        assertFalse(vm.prefs.value.soundEnabled)
    }

    @Test
    fun toggleVibration_updatesState() = runTest {
        val repo = HighScoreRepository()
        val vm = SettingsViewModel(repo)

        vm.toggleVibration(false)
        assertFalse(vm.prefs.value.vibrationEnabled)
    }

    @Test
    fun toggleDarkTheme_updatesState() = runTest {
        val repo = HighScoreRepository()
        val vm = SettingsViewModel(repo)

        vm.toggleDarkTheme(true)
        assertTrue(vm.prefs.value.darkTheme)
    }
}
