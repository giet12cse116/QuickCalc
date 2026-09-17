package com.pp.Quickcalc.domain

import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.model.Operator
import com.pp.Quickcalc.model.RoundState
import kotlin.random.Random

object RoundGenerator {

    fun generate(
        difficulty: Difficulty,
        chain: Int,
        previousAnswer: Int? = null,
        rng: Random = Random.Default
    ): RoundState {
        // Slight ramp: every 5 chain, nudge delta range up within the tier
        val rampBonus = (chain / 5).coerceAtMost(10)

        // Set base to previous question's answer if available, otherwise random within difficulty bounds
        val base = previousAnswer ?: rng.nextInt(difficulty.minBase, difficulty.maxBase + 1)

        // Choose operator; if base is small (<= 2), force PLUS to maintain non-negative growth
        val operator = if (base <= 2) {
            Operator.PLUS
        } else {
            if (rng.nextBoolean()) Operator.PLUS else Operator.MINUS
        }

        val deltaMax = difficulty.maxDelta + rampBonus

        val delta = if (operator == Operator.MINUS) {
            val maxAllowedDelta = (base - 1).coerceAtLeast(1)
            val effectiveMaxDelta = deltaMax.coerceAtMost(maxAllowedDelta)
            val minD = difficulty.minDelta.coerceAtMost(effectiveMaxDelta)
            rng.nextInt(minD.coerceAtLeast(1), effectiveMaxDelta + 1)
        } else {
            rng.nextInt(difficulty.minDelta, deltaMax + 1)
        }

        val correct = if (operator == Operator.PLUS) base + delta else base - delta

        val distractors = buildDistractors(base, delta, operator, correct, rng)
        val options = (listOf(correct) + distractors).shuffled(rng)

        return RoundState(base, operator, delta, options, correct)
    }

    private fun buildDistractors(
        base: Int,
        delta: Int,
        operator: Operator,
        correct: Int,
        rng: Random
    ): List<Int> {
        val opSwap = if (operator == Operator.PLUS) base - delta else base + delta
        val offByOne = correct + (if (rng.nextBoolean()) 1 else -1)
        val operandEcho = base

        val candidates = mutableSetOf(opSwap, offByOne, operandEcho)
        candidates.remove(correct)
        candidates.removeIf { it < 0 }

        var attempts = 0
        while (candidates.size < 3 && attempts < 50) {
            val filler = (correct + rng.nextInt(-4, 5)).coerceAtLeast(0)
            if (filler != correct) candidates.add(filler)
            attempts++
        }

        // Final fallback if still short
        var fallback = correct + 100
        while (candidates.size < 3) {
            if (fallback != correct) candidates.add(fallback)
            fallback++
        }

        return candidates.take(3)
    }
}
