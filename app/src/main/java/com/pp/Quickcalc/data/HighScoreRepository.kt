package com.pp.Quickcalc.data

import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.model.UserPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

import kotlinx.coroutines.flow.map

class HighScoreRepository(
    private val defaultDifficulty: Difficulty = Difficulty.EASY
) : GameRepository {

    private val _difficultyFlow = MutableStateFlow(defaultDifficulty)
    override val difficultyFlow: Flow<Difficulty> = _difficultyFlow.asStateFlow()

    private val _userPrefsFlow = MutableStateFlow(UserPrefs())
    override val userPrefsFlow: Flow<UserPrefs> = _userPrefsFlow.asStateFlow()

    private val levelMap = MutableStateFlow(Difficulty.values().associateWith { 1 })
    private val questionMap = MutableStateFlow(Difficulty.values().associateWith { 1 })

    override fun getLevel(difficulty: Difficulty): Flow<Int> =
        levelMap.map { it[difficulty] ?: 1 }

    override fun getQuestion(difficulty: Difficulty): Flow<Int> =
        questionMap.map { it[difficulty] ?: 1 }

    override suspend fun saveDifficulty(difficulty: Difficulty) {
        _difficultyFlow.value = difficulty
    }

    override suspend fun saveUserPrefs(prefs: UserPrefs) {
        _userPrefsFlow.value = prefs
    }

    override suspend fun saveProgress(difficulty: Difficulty, level: Int, question: Int) {
        levelMap.value = levelMap.value.toMutableMap().apply { put(difficulty, level) }
        questionMap.value = questionMap.value.toMutableMap().apply { put(difficulty, question) }
    }

    override suspend fun resetAllProgress() {
        levelMap.value = Difficulty.values().associateWith { 1 }
        questionMap.value = Difficulty.values().associateWith { 1 }
    }
}
