package com.pp.Quickcalc.domain

import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.model.Operator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoundGeneratorTest {

    private fun rng(seed: Long) = kotlin.random.Random(seed)

    @Test
    fun `options list always has exactly 4 entries`() {
        repeat(1000) { i ->
            val round = RoundGenerator.generate(Difficulty.EASY, chain = 0, rng = rng(i.toLong()))
            assertEquals(4, round.options.size)
        }
    }

    @Test
    fun `options list has no duplicate values`() {
        repeat(1000) { i ->
            val round = RoundGenerator.generate(Difficulty.MEDIUM, chain = 0, rng = rng(i.toLong()))
            assertEquals(round.options.size, round.options.toSet().size)
        }
    }

    @Test
    fun `correct answer is always present in options`() {
        repeat(1000) { i ->
            val round = RoundGenerator.generate(Difficulty.HARD, chain = 0, rng = rng(i.toLong()))
            assertTrue(round.options.contains(round.correctAnswer))
        }
    }

    @Test
    fun `correct answer matches operator applied to base and delta`() {
        repeat(1000) { i ->
            val round = RoundGenerator.generate(Difficulty.EASY, chain = 0, rng = rng(i.toLong()))
            val expected = when (round.operator) {
                Operator.PLUS -> round.baseNumber + round.delta
                Operator.MINUS -> round.baseNumber - round.delta
            }
            assertEquals(expected, round.correctAnswer)
        }
    }

    @Test
    fun `MINUS operator never produces a negative result`() {
        repeat(1000) { i ->
            val round = RoundGenerator.generate(Difficulty.HARD, chain = 0, rng = rng(i.toLong()))
            if (round.operator == Operator.MINUS) {
                assertTrue(
                    "correctAnswer=${round.correctAnswer} should be >= 0",
                    round.correctAnswer >= 0
                )
            }
        }
    }

    @Test
    fun `no distractor equals the correct answer`() {
        repeat(1000) { i ->
            val round = RoundGenerator.generate(Difficulty.MEDIUM, chain = 0, rng = rng(i.toLong()))
            val distractors = round.options.filter { it != round.correctAnswer }
            assertEquals(3, distractors.size)
        }
    }

    @Test
    fun `all options are non-negative`() {
        repeat(1000) { i ->
            val round = RoundGenerator.generate(Difficulty.HARD, chain = 0, rng = rng(i.toLong()))
            round.options.forEach { option ->
                assertTrue("option=$option should be >= 0", option >= 0)
            }
        }
    }

    @Test
    fun `base number stays within difficulty bounds`() {
        val difficulty = Difficulty.MEDIUM
        repeat(1000) { i ->
            val round = RoundGenerator.generate(difficulty, chain = 0, rng = rng(i.toLong()))
            assertTrue(round.baseNumber in difficulty.minBase..difficulty.maxBase)
        }
    }

    @Test
    fun `same seed produces identical round (determinism check)`() {
        val roundA = RoundGenerator.generate(Difficulty.EASY, chain = 3, rng = rng(42L))
        val roundB = RoundGenerator.generate(Difficulty.EASY, chain = 3, rng = rng(42L))
        assertEquals(roundA, roundB)
    }

    @Test
    fun `chain ramp increases max delta range over time`() {
        val lowChainDeltas = (0 until 500).map { i ->
            RoundGenerator.generate(Difficulty.EASY, chain = 0, rng = rng(i.toLong())).delta
        }
        val highChainDeltas = (0 until 500).map { i ->
            RoundGenerator.generate(Difficulty.EASY, chain = 50, rng = rng(i.toLong())).delta
        }

        val avgLow = lowChainDeltas.average()
        val avgHigh = highChainDeltas.average()

        assertTrue(
            "expected higher chain to trend toward larger deltas: low=$avgLow high=$avgHigh",
            avgHigh >= avgLow
        )
    }

    @Test
    fun `distractor generation never crashes on edge-case small base`() {
        repeat(2000) { i ->
            val round = RoundGenerator.generate(Difficulty.EASY, chain = 0, rng = rng(i.toLong()))
            assertEquals(4, round.options.size)
            assertTrue(round.correctAnswer >= 0)
        }
    }

    @Test
    fun `distractor generation terminates for tiny correct answers`() {
        repeat(500) { i ->
            val round = RoundGenerator.generate(Difficulty.EASY, chain = 0, rng = rng(i.toLong()))
            assertEquals(4, round.options.size)
        }
    }

    @Test
    fun `previous answer becomes base number of next round`() {
        val firstRound = RoundGenerator.generate(Difficulty.EASY, chain = 0, rng = rng(100L))
        val answer = firstRound.correctAnswer
        val secondRound = RoundGenerator.generate(Difficulty.EASY, chain = 1, previousAnswer = answer, rng = rng(101L))
        assertEquals(answer, secondRound.baseNumber)
    }

    @Test
    fun `difficulty timer durations match requirements`() {
        assertEquals(7_000L, Difficulty.EASY.roundTimeMs)
        assertEquals(10_000L, Difficulty.MEDIUM.roundTimeMs)
        assertEquals(15_000L, Difficulty.HARD.roundTimeMs)
    }
}
