package com.pp.Quickcalc.model

enum class Difficulty(
    val minBase: Int,
    val maxBase: Int,
    val minDelta: Int,
    val maxDelta: Int,
    val roundTimeMs: Long
) {
    EASY(5, 50, 3, 15, 7_000L),
    MEDIUM(10, 100, 5, 30, 10_000L),
    HARD(20, 200, 10, 60, 15_000L);

    val displayName: String
        get() = name.lowercase().replaceFirstChar { it.uppercase() }
}
