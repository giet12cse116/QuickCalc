package com.pp.Quickcalc.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pp.Quickcalc.data.GameRepository
import com.pp.Quickcalc.data.HighScoreRepository
import com.pp.Quickcalc.model.UserPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: GameRepository
) : ViewModel() {

    val prefs: StateFlow<UserPrefs> = repo.userPrefsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPrefs())

    fun toggleSound(enabled: Boolean) = updatePrefs { it.copy(soundEnabled = enabled) }
    fun toggleVibration(enabled: Boolean) = updatePrefs { it.copy(vibrationEnabled = enabled) }
    fun toggleDarkTheme(enabled: Boolean) = updatePrefs { it.copy(darkTheme = enabled) }

    fun resetGameProgress() {
        viewModelScope.launch {
            repo.resetAllProgress()
        }
    }

    private fun updatePrefs(transform: (UserPrefs) -> UserPrefs) {
        viewModelScope.launch {
            repo.saveUserPrefs(transform(prefs.value))
        }
    }
}

class SettingsViewModelFactory(
    private val repo: GameRepository = HighScoreRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(repo) as T
    }
}
