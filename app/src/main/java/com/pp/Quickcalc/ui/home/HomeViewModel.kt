package com.pp.Quickcalc.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pp.Quickcalc.data.GameRepository
import com.pp.Quickcalc.data.HighScoreRepository
import com.pp.Quickcalc.model.Difficulty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: GameRepository
) : ViewModel() {

    val difficulty: StateFlow<Difficulty> = repo.difficultyFlow.stateIn(viewModelScope, SharingStarted.Eagerly, Difficulty.EASY)

    private val _isInfoModalVisible = MutableStateFlow(false)
    val isInfoModalVisible: StateFlow<Boolean> = _isInfoModalVisible.asStateFlow()

    fun setDifficulty(difficulty: Difficulty) {
        viewModelScope.launch {
            repo.saveDifficulty(difficulty)
        }
    }

    fun toggleInfoModal(visible: Boolean) {
        _isInfoModalVisible.value = visible
    }
}

class HomeViewModelFactory(
    private val repo: GameRepository = HighScoreRepository()
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(repo) as T
    }
}
