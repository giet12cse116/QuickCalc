package com.pp.Quickcalc.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pp.Quickcalc.QuickcalcApplication
import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.ui.components.InfoDialog
import com.pp.Quickcalc.ui.game.GameScreen
import com.pp.Quickcalc.ui.game.GameViewModel
import com.pp.Quickcalc.ui.gameover.GameOverScreen
import com.pp.Quickcalc.ui.home.HomeScreen
import com.pp.Quickcalc.ui.settings.SettingsScreen
import com.pp.Quickcalc.ui.settings.SettingsViewModel
import com.pp.Quickcalc.ui.splash.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Settings : Screen("settings")
    object Game : Screen("game/{difficulty}") {
        fun createRoute(difficulty: Difficulty) = "game/${difficulty.name}"
    }
    object GameOver : Screen("gameOver/{chain}/{difficulty}") {
        fun createRoute(chain: Int, difficulty: Difficulty) = "gameOver/$chain/${difficulty.name}"
    }
}

@Composable
fun NumflowNavGraph(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            var showInfoModal by remember { mutableStateOf(false) }

            HomeScreen(
                onPlayClick = { selectedDifficulty ->
                    navController.navigate(Screen.Game.createRoute(selectedDifficulty))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onInfoClick = {
                    showInfoModal = true
                }
            )

            if (showInfoModal) {
                InfoDialog(onDismiss = { showInfoModal = false })
            }
        }

        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = hiltViewModel()
            val prefs by vm.prefs.collectAsState()

            SettingsScreen(
                prefs = prefs,
                onSoundToggle = vm::toggleSound,
                onVibrationToggle = vm::toggleVibration,
                onThemeToggle = vm::toggleDarkTheme,
                onResetProgress = vm::resetGameProgress,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            Screen.Game.route,
            arguments = listOf(
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val difficultyArg = backStackEntry.arguments?.getString("difficulty") ?: Difficulty.EASY.name
            val difficulty = Difficulty.valueOf(difficultyArg)

            val vm: GameViewModel = hiltViewModel()
            val uiState by vm.uiState.collectAsState()

            val appOpenAdManager = remember { QuickcalcApplication.instance.appOpenAdManager }
            DisposableEffect(Unit) {
                appOpenAdManager.isGameplayActive = true
                onDispose {
                    appOpenAdManager.isGameplayActive = false
                }
            }

            GameScreen(
                difficulty = difficulty,
                onBackToHome = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                viewModel = vm
            )

            if (uiState.isGameOver) {
                navController.navigate(
                    Screen.GameOver.createRoute(uiState.chain, difficulty)
                ) {
                    popUpTo(Screen.Home.route)
                }
            }
        }

        composable(
            Screen.GameOver.route,
            arguments = listOf(
                navArgument("chain") { type = NavType.IntType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chain = backStackEntry.arguments?.getInt("chain") ?: 0
            val difficultyArg = backStackEntry.arguments?.getString("difficulty") ?: Difficulty.EASY.name
            val difficulty = Difficulty.valueOf(difficultyArg)

            GameOverScreen(
                finalChain = chain,
                onPlayAgain = {
                    navController.navigate(Screen.Game.createRoute(difficulty)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onHomeClick = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }
    }
}
