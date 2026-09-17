package com.pp.Quickcalc.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.model.UserPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @ApplicationContext private val context: Context
) : GameRepository {

    private object Keys {
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val SOUND_ON = booleanPreferencesKey("sound_on")
        val VIBRATION_ON = booleanPreferencesKey("vibration_on")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val HAS_SHOWN_RATE_PROMPT = booleanPreferencesKey("has_shown_rate_prompt")

        fun levelKey(difficulty: Difficulty) = intPreferencesKey("level_${difficulty.name}")
        fun questionKey(difficulty: Difficulty) = intPreferencesKey("question_${difficulty.name}")
    }

    override val difficultyFlow: Flow<Difficulty> =
        dataStore.data.map { p ->
            val name = p[Keys.DIFFICULTY] ?: Difficulty.EASY.name
            try {
                Difficulty.valueOf(name)
            } catch (e: Exception) {
                Difficulty.EASY
            }
        }

    override val userPrefsFlow: Flow<UserPrefs> =
        dataStore.data.map { p ->
            UserPrefs(
                soundEnabled = p[Keys.SOUND_ON] ?: true,
                vibrationEnabled = p[Keys.VIBRATION_ON] ?: true,
                darkTheme = p[Keys.DARK_THEME] ?: false,
                hasShownRatePrompt = p[Keys.HAS_SHOWN_RATE_PROMPT] ?: false
            )
        }

    override fun getLevel(difficulty: Difficulty): Flow<Int> =
        dataStore.data.map { p -> p[Keys.levelKey(difficulty)] ?: 1 }

    override fun getQuestion(difficulty: Difficulty): Flow<Int> =
        dataStore.data.map { p -> p[Keys.questionKey(difficulty)] ?: 1 }

    override suspend fun saveDifficulty(difficulty: Difficulty) {
        dataStore.edit { it[Keys.DIFFICULTY] = difficulty.name }
    }

    override suspend fun saveUserPrefs(prefs: UserPrefs) {
        dataStore.edit {
            it[Keys.SOUND_ON] = prefs.soundEnabled
            it[Keys.VIBRATION_ON] = prefs.vibrationEnabled
            it[Keys.DARK_THEME] = prefs.darkTheme
            it[Keys.HAS_SHOWN_RATE_PROMPT] = prefs.hasShownRatePrompt
        }
    }

    override suspend fun saveProgress(difficulty: Difficulty, level: Int, question: Int) {
        dataStore.edit { p ->
            p[Keys.levelKey(difficulty)] = level
            p[Keys.questionKey(difficulty)] = question
        }
    }

    override suspend fun resetAllProgress() {
        dataStore.edit { p ->
            Difficulty.values().forEach { d ->
                p[Keys.levelKey(d)] = 1
                p[Keys.questionKey(d)] = 1
            }
        }
    }
}
