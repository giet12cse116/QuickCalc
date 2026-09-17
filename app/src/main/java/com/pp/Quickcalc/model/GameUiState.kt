package com.pp.Quickcalc.model

sealed interface GameEvent {
    data class OptionSelected(val value: Int) : GameEvent
    object TimeUp : GameEvent
    object AddTimeClicked : GameEvent
    object WatchAdForHeartClicked : GameEvent
    object UseHeartForTimeClicked : GameEvent
    object WatchAdForTimeClicked : GameEvent
    object WrongAnswerContinueClicked : GameEvent
    object LevelCompletedContinueClicked : GameEvent
    object DismissModalsClicked : GameEvent
    object RateNowClicked : GameEvent
    object DismissRateModalClicked : GameEvent
}

enum class Feedback { CORRECT, WRONG }

data class GameUiState(
    val currentLevel: Int = 1,
    val currentQuestion: Int = 1,
    val score: Int = 0,
    val hearts: Int = 3,
    val chain: Int = 0,
    val difficulty: Difficulty = Difficulty.EASY,
    val round: RoundState,
    val timeFraction: Float = 1f,
    val secondsLeft: Int = 0,
    val disabledOptions: Set<Int> = emptySet(),
    val wrongQuestions: Set<Int> = emptySet(),
    val showWrongAnswerModal: Boolean = false,
    val showOutofLivesModal: Boolean = false,
    val showTimeOutModal: Boolean = false,
    val showLevelCompletedModal: Boolean = false,
    val completedLevelNumber: Int = 1,
    val showReadyBufferModal: Boolean = false,
    val readyCountdownSeconds: Int = 3,
    val showRateModal: Boolean = false,
    val isGameOver: Boolean = false,
    val feedback: Feedback? = null
) {
    val currentQuestionIndex: Int
        get() = (currentLevel - 1) * 20 + currentQuestion
}
