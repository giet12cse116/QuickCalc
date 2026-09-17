package com.pp.Quickcalc.data

import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.model.UserPrefs
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    val difficultyFlow: Flow<Difficulty>
    val userPrefsFlow: Flow<UserPrefs>

    fun getLevel(difficulty: Difficulty): Flow<Int>
    fun getQuestion(difficulty: Difficulty): Flow<Int>

    suspend fun saveDifficulty(difficulty: Difficulty)
    suspend fun saveUserPrefs(prefs: UserPrefs)
    suspend fun saveProgress(difficulty: Difficulty, level: Int, question: Int)
    suspend fun resetAllProgress()
}
