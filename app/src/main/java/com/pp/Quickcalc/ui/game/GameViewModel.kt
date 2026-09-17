package com.pp.Quickcalc.ui.game

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pp.Quickcalc.audio.FeedbackManager
import com.pp.Quickcalc.data.GameRepository
import com.pp.Quickcalc.data.HighScoreRepository
import com.pp.Quickcalc.domain.RoundGenerator
import com.pp.Quickcalc.domain.TimerController
import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.model.Feedback
import com.pp.Quickcalc.model.GameEvent
import com.pp.Quickcalc.model.GameUiState
import com.pp.Quickcalc.model.RoundState
import com.pp.Quickcalc.model.UserPrefs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.first

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repo: GameRepository,
    private val feedbackManager: FeedbackManager?,
    private val savedStateHandle: SavedStateHandle,
    private val analytics: FirebaseAnalytics? = null
) : ViewModel() {

    private val difficulty: Difficulty = Difficulty.valueOf(
        savedStateHandle.get<String>("difficulty") ?: Difficulty.EASY.name
    )

    private val timer = TimerController(viewModelScope)
    private val rng = kotlin.random.Random.Default
    private var userPrefs = UserPrefs()

    // Restore state from SavedStateHandle if present
    private val restoredChain: Int = savedStateHandle.get<Int>("chain") ?: 0
    private val restoredScore: Int = savedStateHandle.get<Int>("score") ?: 0
    private val restoredHearts: Int = savedStateHandle.get<Int>("hearts") ?: 3
    private val restoredLevel: Int? = savedStateHandle.get<Int>("level")
    private val restoredQuestion: Int? = savedStateHandle.get<Int>("question")
    private val restoredRound: RoundState? = savedStateHandle.get<RoundState>("round")
    private val restoredTimeLeft: Long = savedStateHandle.get<Long>("timeLeft") ?: difficulty.roundTimeMs

    private val _uiState = MutableStateFlow(
        GameUiState(
            currentLevel = restoredLevel ?: 1,
            currentQuestion = 1,
            score = restoredScore,
            hearts = restoredHearts,
            chain = restoredChain,
            round = RoundGenerator.generate(difficulty, restoredChain, previousAnswer = null, rng = rng),
            difficulty = difficulty,
            secondsLeft = (difficulty.roundTimeMs / 1000L).toInt()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var readyJob: kotlinx.coroutines.Job? = null

    private fun logAnalyticsEvent(eventName: String, params: Bundle.() -> Unit = {}) {
        try {
            val bundle = Bundle().apply(params)
            analytics?.logEvent(eventName, bundle)
        } catch (_: Exception) {}
    }

    init {
        viewModelScope.launch {
            repo.userPrefsFlow.collect { prefs ->
                userPrefs = prefs
            }
        }

        viewModelScope.launch {
            val savedL = repo.getLevel(difficulty).first()
            val targetLevel = restoredLevel ?: savedL
            val initialRound = RoundGenerator.generate(difficulty, 0, previousAnswer = null, rng = rng)
            _uiState.value = _uiState.value.copy(
                currentLevel = targetLevel,
                currentQuestion = 1,
                round = initialRound
            )
            logAnalyticsEvent(FirebaseAnalytics.Event.LEVEL_START) {
                putString(FirebaseAnalytics.Param.LEVEL_NAME, "Level $targetLevel")
                putString("difficulty", difficulty.name)
            }
            triggerReadyBuffer()
        }

        viewModelScope.launch {
            while (true) {
                delay(50L)
                val leftMs = timer.timeLeftMs.value
                val sec = ((leftMs + 999L) / 1000L).toInt()
                _uiState.value = _uiState.value.copy(
                    timeFraction = timer.progressFraction(),
                    secondsLeft = sec
                )
                persistState()
            }
        }
    }

    private fun triggerReadyBuffer(onComplete: () -> Unit = { startRoundTimer() }) {
        timer.stop()
        readyJob?.cancel()
        readyJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showReadyBufferModal = true,
                readyCountdownSeconds = 3
            )
            delay(1000L)
            _uiState.value = _uiState.value.copy(readyCountdownSeconds = 2)
            delay(1000L)
            _uiState.value = _uiState.value.copy(readyCountdownSeconds = 1)
            delay(1000L)
            _uiState.value = _uiState.value.copy(readyCountdownSeconds = 0)
            delay(300L)
            _uiState.value = _uiState.value.copy(showReadyBufferModal = false)
            onComplete()
        }
    }

    private fun startRoundTimer(timeMs: Long = difficulty.roundTimeMs) {
        timer.start(timeMs) { onEvent(GameEvent.TimeUp) }
    }

    private fun persistState() {
        val current = _uiState.value
        savedStateHandle["chain"] = current.chain
        savedStateHandle["score"] = current.score
        savedStateHandle["hearts"] = current.hearts
        savedStateHandle["level"] = current.currentLevel
        savedStateHandle["question"] = current.currentQuestion
        savedStateHandle["round"] = current.round
        savedStateHandle["timeLeft"] = timer.timeLeftMs.value
    }

    private fun saveProgressToRepo(level: Int, question: Int) {
        viewModelScope.launch {
            repo.saveProgress(difficulty, level, question)
        }
    }

    private fun getNextLevelAndQuestion(level: Int, question: Int): Pair<Int, Int> {
        return if (question >= 20) {
            if (level >= 20) {
                Pair(20, 20)
            } else {
                Pair(level + 1, 1)
            }
        } else {
            Pair(level, question + 1)
        }
    }

    fun onEvent(event: GameEvent) {
        when (event) {
            is GameEvent.OptionSelected -> handleAnswer(event.value)
            GameEvent.TimeUp -> handleTimeUp()
            GameEvent.AddTimeClicked -> {
                timer.addTime(bonusMs = 5_000L)
                persistState()
            }
            GameEvent.WatchAdForHeartClicked -> grantHeartFromAd()
            GameEvent.UseHeartForTimeClicked -> reviveWithHeart()
            GameEvent.WatchAdForTimeClicked -> reviveWithAdTime()
            GameEvent.WrongAnswerContinueClicked -> handleWrongAnswerContinue()
            GameEvent.LevelCompletedContinueClicked -> handleLevelCompletedContinue()
            GameEvent.DismissModalsClicked -> endGame()
            GameEvent.RateNowClicked -> {
                _uiState.value = _uiState.value.copy(showRateModal = false)
                markRatePromptShown()
                triggerReadyBuffer()
            }
            GameEvent.DismissRateModalClicked -> {
                _uiState.value = _uiState.value.copy(showRateModal = false)
                markRatePromptShown()
                triggerReadyBuffer()
            }
        }
    }

    private fun markRatePromptShown() {
        viewModelScope.launch {
            val updated = userPrefs.copy(hasShownRatePrompt = true)
            userPrefs = updated
            repo.saveUserPrefs(updated)
        }
    }

    private var isAnswerProcessing = false

    private fun handleAnswer(selected: Int) {
        val current = _uiState.value
        if (isAnswerProcessing || current.showWrongAnswerModal || current.showOutofLivesModal || current.showTimeOutModal || current.showLevelCompletedModal || current.showReadyBufferModal || current.showRateModal) {
            return
        }

        val correct = selected == current.round.correctAnswer

        if (correct) {
            isAnswerProcessing = true
            timer.stop()

            feedbackManager?.playCorrect(userPrefs.soundEnabled)
            feedbackManager?.vibrateCorrect(userPrefs.vibrationEnabled)

            val difficultySec = (difficulty.roundTimeMs / 1000L).toInt()
            val timeTakenSec = maxOf(0, difficultySec - current.secondsLeft)
            val earnedPoints = maxOf(1, difficultySec - timeTakenSec)

            val newScore = current.score + earnedPoints
            val newChain = current.chain + 1

            // Color the correct answer green immediately and disable option taps during 1 sec delay
            _uiState.value = current.copy(
                score = newScore,
                chain = newChain,
                disabledOptions = current.round.options.toSet(),
                feedback = Feedback.CORRECT
            )

            viewModelScope.launch {
                delay(1000L) // Wait 1 second before moving to next question
                isAnswerProcessing = false

                val stateAfterDelay = _uiState.value
                if (stateAfterDelay.currentQuestion >= 20) {
                    // Level completed!
                    feedbackManager?.playLevelComplete(userPrefs.soundEnabled)
                    feedbackManager?.vibrateLevelComplete(userPrefs.vibrationEnabled)
                    logAnalyticsEvent(FirebaseAnalytics.Event.LEVEL_UP) {
                        putInt(FirebaseAnalytics.Param.LEVEL, stateAfterDelay.currentLevel)
                        putString("difficulty", difficulty.name)
                    }
                    _uiState.value = stateAfterDelay.copy(
                        showLevelCompletedModal = true,
                        completedLevelNumber = stateAfterDelay.currentLevel,
                        feedback = null
                    )
                    saveProgressToRepo(stateAfterDelay.currentLevel, stateAfterDelay.currentQuestion)
                } else {
                    val nextRound = RoundGenerator.generate(
                        difficulty = difficulty,
                        chain = newChain,
                        previousAnswer = current.round.correctAnswer,
                        rng = rng
                    )
                    val (nextLevel, nextQuestion) = getNextLevelAndQuestion(stateAfterDelay.currentLevel, stateAfterDelay.currentQuestion)
                    _uiState.value = stateAfterDelay.copy(
                        disabledOptions = emptySet(),
                        currentLevel = nextLevel,
                        currentQuestion = nextQuestion,
                        round = nextRound,
                        feedback = null
                    )
                    saveProgressToRepo(nextLevel, nextQuestion)
                    startRoundTimer()
                }
                persistState()
            }
        } else {
            feedbackManager?.playWrong(userPrefs.soundEnabled)
            feedbackManager?.vibrateWrong(userPrefs.vibrationEnabled)

            val newHearts = current.hearts - 1
            val updatedWrongQuestions = current.wrongQuestions + current.currentQuestion

            timer.stop()

            if (newHearts > 0) {
                _uiState.value = current.copy(
                    hearts = newHearts,
                    wrongQuestions = updatedWrongQuestions,
                    showWrongAnswerModal = true,
                    feedback = Feedback.WRONG
                )
            } else {
                _uiState.value = current.copy(
                    hearts = 0,
                    wrongQuestions = updatedWrongQuestions,
                    showOutofLivesModal = true,
                    feedback = null
                )
            }
        }
        persistState()
    }

    private fun handleLevelCompletedContinue() {
        val current = _uiState.value
        val nextLevel = if (current.currentLevel >= 20) 20 else current.currentLevel + 1
        val nextQuestion = 1
        val nextRound = RoundGenerator.generate(
            difficulty = difficulty,
            chain = current.chain,
            previousAnswer = current.round.correctAnswer,
            rng = rng
        )

        val shouldShowRateModal = current.completedLevelNumber >= 3 && !userPrefs.hasShownRatePrompt

        if (shouldShowRateModal) {
            _uiState.value = current.copy(
                showLevelCompletedModal = false,
                showRateModal = true,
                disabledOptions = emptySet(),
                wrongQuestions = emptySet(),
                currentLevel = nextLevel,
                currentQuestion = nextQuestion,
                round = nextRound,
                feedback = null
            )
            markRatePromptShown()
            saveProgressToRepo(nextLevel, nextQuestion)
            persistState()
            return
        }

        _uiState.value = current.copy(
            showLevelCompletedModal = false,
            disabledOptions = emptySet(),
            wrongQuestions = emptySet(),
            currentLevel = nextLevel,
            currentQuestion = nextQuestion,
            round = nextRound,
            feedback = null
        )
        saveProgressToRepo(nextLevel, nextQuestion)
        persistState()
        triggerReadyBuffer()
    }

    private fun handleWrongAnswerContinue() {
        val current = _uiState.value
        val (nextLevel, nextQuestion) = getNextLevelAndQuestion(current.currentLevel, current.currentQuestion)
        val isNewLevel = nextLevel != current.currentLevel

        val nextRound = RoundGenerator.generate(
            difficulty = difficulty,
            chain = current.chain,
            previousAnswer = current.round.correctAnswer,
            rng = rng
        )
        _uiState.value = current.copy(
            showWrongAnswerModal = false,
            disabledOptions = emptySet(),
            wrongQuestions = if (isNewLevel) emptySet() else current.wrongQuestions,
            currentLevel = nextLevel,
            currentQuestion = nextQuestion,
            round = nextRound,
            feedback = null
        )
        saveProgressToRepo(nextLevel, nextQuestion)
        persistState()

        if (isNewLevel) {
            triggerReadyBuffer()
        } else {
            startRoundTimer()
        }
    }

    private fun handleTimeUp() {
        timer.stop()
        val current = _uiState.value
        _uiState.value = current.copy(
            wrongQuestions = current.wrongQuestions + current.currentQuestion,
            showTimeOutModal = true,
            feedback = null
        )
        persistState()
    }

    private fun grantHeartFromAd() {
        _uiState.value = _uiState.value.copy(
            showOutofLivesModal = false,
            feedback = null,
            disabledOptions = emptySet()
        )
        persistState()
        startRoundTimer(7_000L)
    }

    private fun reviveWithHeart() {
        val current = _uiState.value
        if (current.hearts > 0) {
            _uiState.value = current.copy(
                hearts = current.hearts - 1,
                showTimeOutModal = false,
                feedback = null,
                disabledOptions = emptySet()
            )
            startRoundTimer()
            persistState()
        }
    }

    private fun reviveWithAdTime() {
        _uiState.value = _uiState.value.copy(
            showTimeOutModal = false,
            feedback = null,
            disabledOptions = emptySet()
        )
        startRoundTimer(7_000L)
        persistState()
    }

    private fun endGame() {
        timer.stop()
        _uiState.value = _uiState.value.copy(
            isGameOver = true,
            showOutofLivesModal = false,
            showTimeOutModal = false
        )
        persistState()
    }

    fun restartGame() {
        timer.stop()
        savedStateHandle.remove<Int>("chain")
        savedStateHandle.remove<Int>("score")
        savedStateHandle.remove<Int>("hearts")
        savedStateHandle.remove<Int>("level")
        savedStateHandle.remove<Int>("question")
        savedStateHandle.remove<RoundState>("round")
        savedStateHandle.remove<Long>("timeLeft")

        val initialRound = RoundGenerator.generate(difficulty, 0, previousAnswer = null, rng = rng)
        _uiState.value = GameUiState(
            currentLevel = 1,
            currentQuestion = 1,
            score = 0,
            hearts = 3,
            chain = 0,
            difficulty = difficulty,
            round = initialRound,
            timeFraction = 1f,
            secondsLeft = (difficulty.roundTimeMs / 1000L).toInt(),
            showOutofLivesModal = false,
            showTimeOutModal = false,
            isGameOver = false,
            feedback = null
        )
        saveProgressToRepo(1, 1)
        persistState()
        triggerReadyBuffer()
    }

    override fun onCleared() {
        timer.stop()
        feedbackManager?.release()
        super.onCleared()
    }
}

class GameViewModelFactory(
    private val repo: GameRepository = HighScoreRepository(),
    private val feedbackManager: FeedbackManager? = null,
    private val difficulty: Difficulty
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val handle = SavedStateHandle(mapOf("difficulty" to difficulty.name))
        return GameViewModel(repo, feedbackManager, handle) as T
    }
}
