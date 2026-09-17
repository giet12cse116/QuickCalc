package com.pp.Quickcalc.ui.gameover

import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Test

class GameOverScreenTest {

    @Test
    fun screenGame_createRoute_formatsCorrectly() {
        val route = Screen.Game.createRoute(Difficulty.HARD)
        assertEquals("game/HARD", route)
    }

    @Test
    fun screenGameOver_createRoute_formatsAllArguments() {
        val route = Screen.GameOver.createRoute(chain = 15, difficulty = Difficulty.MEDIUM)
        assertEquals("gameOver/15/MEDIUM", route)
    }
}
