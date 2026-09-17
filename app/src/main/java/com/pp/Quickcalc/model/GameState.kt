package com.pp.Quickcalc.model

enum class GameStatus {
    READY,
    PLAYING,
    GAME_OVER
}

data class GameState(
    val chain: Int = 0,
    val timeRemainingMs: Long = 0L,
    val maxTimeMs: Long = 6000L,
    val difficulty: Difficulty = Difficulty.EASY,
    val round: RoundState? = null,
    val status: GameStatus = GameStatus.READY,
    val lastAnswerWasCorrect: Boolean? = null
) {
    val progress: Float
        get() = if (maxTimeMs > 0) (timeRemainingMs.toFloat() / maxTimeMs.toFloat()).coerceIn(0f, 1f) else 0f
}
