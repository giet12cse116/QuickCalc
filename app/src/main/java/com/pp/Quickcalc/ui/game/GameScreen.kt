package com.pp.Quickcalc.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.model.Feedback
import com.pp.Quickcalc.model.GameEvent
import com.pp.Quickcalc.model.GameUiState
import com.pp.Quickcalc.model.Operator
import com.pp.Quickcalc.ui.components.AdBannerPlaceholder
import com.pp.Quickcalc.ui.components.rotatingGradientBorder
import com.pp.Quickcalc.ui.theme.DangerRed
import com.pp.Quickcalc.ui.theme.DarkBackground
import com.pp.Quickcalc.ui.theme.NeonCyan
import com.pp.Quickcalc.ui.theme.NeonPurple
import com.pp.Quickcalc.ui.theme.SurfaceCard
import com.pp.Quickcalc.ui.theme.SurfaceCardBorder
import com.pp.Quickcalc.ui.theme.TextSecondary
import com.pp.Quickcalc.ui.theme.WarningYellow

import android.app.Activity
import com.pp.Quickcalc.ads.InterstitialAdManager
import com.pp.Quickcalc.ads.RewardedAdManager
import androidx.compose.ui.platform.LocalContext
import com.pp.Quickcalc.ui.settings.openPlayStore

@Composable
fun timeBasedBrush(timeFraction: Float, darkTheme: Boolean = isSystemInDarkTheme()): Brush {
    val fraction = (1f - timeFraction).coerceIn(0f, 1f)
    return if (darkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                lerp(Color(0xFF1B4332), Color(0xFF3D3A1F), fraction),
                lerp(Color(0xFF3D3A1F), Color(0xFF4A1F1F), fraction)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                lerp(Color(0xFFB9F6CA), Color(0xFFFFF9C4), fraction),
                lerp(Color(0xFFFFF9C4), Color(0xFFFFCDD2), fraction)
            )
        )
    }
}

@Composable
fun GameScreen(
    difficulty: Difficulty,
    onBackToHome: () -> Unit,
    viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(difficulty = difficulty)),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    GameScreenContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onGameOver = { },
        onBackToHome = onBackToHome,
        onRestart = viewModel::restartGame,
        modifier = modifier
    )
}

@Composable
fun GameScreenContent(
    uiState: GameUiState,
    onEvent: (GameEvent) -> Unit,
    onGameOver: () -> Unit,
    onBackToHome: () -> Unit,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val interstitialAdManager = remember(context) { InterstitialAdManager(context) }
    val rewardedAdManager = remember(context) { RewardedAdManager(context) }

    LaunchedEffect(uiState.isGameOver) {
        if (uiState.isGameOver) onGameOver()
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFFFFF3E0))) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top bar: Level, Score, Hearts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✕",
                    color = Color(0xFF2D2A4A),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBackToHome() }
                        .padding(6.dp)
                )

                DifficultyPill(difficulty = uiState.difficulty)
                ChainPill(chain = uiState.chain)
                ScorePill(score = uiState.score)
                HeartsPill(hearts = uiState.hearts)
            }

            // Level & Question Progress Rows (At the top of the TIMER)
            LevelAndQuestionBars(
                currentLevel = uiState.currentLevel,
                currentQuestion = uiState.currentQuestion,
                wrongQuestions = uiState.wrongQuestions
            )

            // Standalone Large Timer Count (Outside progress bar, 32sp large font size matching options)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⏱️", fontSize = 24.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${uiState.secondsLeft}s",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = if (uiState.secondsLeft <= 3) Color(0xFFEF4444) else Color(0xFF2D2A4A)
                    )
                }
            }

            // Sleek Progress Bar
            TimeProgressBar(
                fraction = uiState.timeFraction,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(10.dp)
            )

            // Center main text in exact middle of screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("game_screen_number_display"),
                contentAlignment = Alignment.Center
            ) {
                NumberDisplay(
                    base = uiState.round.baseNumber,
                    operator = uiState.round.operator,
                    delta = uiState.round.delta
                )
            }

            // Options grid
            OptionsGrid(
                options = uiState.round.options,
                feedback = uiState.feedback,
                correctAnswer = uiState.round.correctAnswer,
                disabledOptions = uiState.disabledOptions,
                onOptionClick = { value -> onEvent(GameEvent.OptionSelected(value)) }
            )

            Spacer(Modifier.height(12.dp))

            // Advertisement moved to bottom of the screen
            AdBannerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            )
        }

        // Wrong Answer Heart Deducted Overlay Modal
        if (uiState.showWrongAnswerModal) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, DangerRed, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "❌", fontSize = 44.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "WRONG ANSWER!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = DangerRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "-1 Heart ❤️ Used!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = WarningYellow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Moving to next question...",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { onEvent(GameEvent.WrongAnswerContinueClicked) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "CONTINUE ▶",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Out of Lives / Lifeline Exhausted Dialog
        if (uiState.showOutofLivesModal) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, DangerRed, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "💔", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "LIFELINE EXHAUSTED!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = DangerRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Watch a short ad to add +7 seconds and continue playing!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val activity = context as? Activity
                                rewardedAdManager.show(activity) {
                                    onEvent(GameEvent.WatchAdForHeartClicked)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "WATCH AD 🎬 (+7 SEC)",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        if (uiState.hearts <= 0) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onEvent(GameEvent.DismissModalsClicked) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
                            ) {
                                Text(text = "GIVE UP", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Time Out Revival Dialog
        if (uiState.showTimeOutModal) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, WarningYellow, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "⏰", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "TIME'S UP!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = WarningYellow
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Utilize a heart or watch an ad to get extra time!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        if (uiState.hearts > 0) {
                            Button(
                                onClick = { onEvent(GameEvent.UseHeartForTimeClicked) },
                                colors = ButtonDefaults.buttonColors(containerColor = WarningYellow),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "USE HEART ❤️ (+${uiState.difficulty.roundTimeMs / 1000} SEC)",
                                    color = DarkBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Button(
                            onClick = {
                                val activity = context as? Activity
                                rewardedAdManager.show(activity) {
                                    onEvent(GameEvent.WatchAdForTimeClicked)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "WATCH AD 🎬 (+7 SEC)",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        if (uiState.hearts <= 0) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { onEvent(GameEvent.DismissModalsClicked) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
                            ) {
                                Text(text = "GIVE UP", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Level Completed Dialog Modal
        if (uiState.showLevelCompletedModal) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(0xFF22C55E), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎉", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "LEVEL ${uiState.completedLevelNumber} COMPLETED!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF22C55E),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Great job! You completed Level ${uiState.completedLevelNumber}.",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val activity = context as? Activity
                                if (uiState.completedLevelNumber % 3 == 0 && activity != null) {
                                    interstitialAdManager.showAd(activity) {
                                        onEvent(GameEvent.LevelCompletedContinueClicked)
                                    }
                                } else {
                                    onEvent(GameEvent.LevelCompletedContinueClicked)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "CONTINUE ▶",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // Rate App Modal Dialog
        if (uiState.showRateModal) {
            Dialog(onDismissRequest = { onEvent(GameEvent.DismissRateModalClicked) }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, WarningYellow, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⭐", fontSize = 28.sp)
                            Text(
                                text = "✕",
                                color = TextSecondary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onEvent(GameEvent.DismissRateModalClicked) }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ENJOYING QUICKCALC?",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = WarningYellow,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "⭐⭐⭐⭐⭐",
                            fontSize = 24.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "You're doing great! Please take a moment to rate QuickCalc on the Play Store. Your support means the world to us!",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                openPlayStore(context)
                                onEvent(GameEvent.RateNowClicked)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WarningYellow),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "RATE NOW ⭐",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { onEvent(GameEvent.DismissRateModalClicked) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
                        ) {
                            Text(text = "NO THANKS", color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // 3-Second Ready Buffer Countdown Dialog Modal
        if (uiState.showReadyBufferModal) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1B4B)),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .border(2.dp, Brush.linearGradient(listOf(NeonCyan, NeonPurple)), RoundedCornerShape(28.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = NeonCyan.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "LEVEL ${uiState.currentLevel}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCyan,
                                letterSpacing = 2.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "GET READY!",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            WarningYellow.copy(alpha = 0.25f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .border(2.dp, WarningYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (uiState.readyCountdownSeconds > 0) "${uiState.readyCountdownSeconds}" else "GO!",
                                fontSize = if (uiState.readyCountdownSeconds > 0) 52.sp else 36.sp,
                                fontWeight = FontWeight.Black,
                                color = WarningYellow,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // Game Over Overlay Modal
        if (uiState.isGameOver) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, DangerRed, RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "GAME OVER",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = DangerRed
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "FINAL SCORE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = "${uiState.score}",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Black,
                            color = WarningYellow
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onRestart,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "PLAY AGAIN 🔄",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = onBackToHome,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(14.dp))
                        ) {
                            Text(
                                text = "MAIN MENU",
                                color = TextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultyPill(difficulty: Difficulty) {
    Surface(shape = RoundedCornerShape(20.dp), color = SurfaceCard) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = difficulty.displayName.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                color = NeonCyan
            )
        }
    }
}

@Composable
fun ScorePill(score: Int) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.6f)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("PTS", fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = Color(0xFF2D2A4A))
            Spacer(Modifier.width(4.dp))
            Text("$score", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF2D2A4A))
        }
    }
}

@Composable
fun HeartsPill(hearts: Int) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.6f)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("❤️", fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
            Text("$hearts", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF2D2A4A))
        }
    }
}

@Composable
fun ChainPill(chain: Int) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(alpha = 0.6f)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚡", fontSize = 13.sp)
            Spacer(Modifier.width(4.dp))
            Text("$chain", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF2D2A4A))
        }
    }
}

@Composable
fun TimeProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    val animatedFraction by animateFloatAsState(targetValue = fraction.coerceIn(0f, 1f), label = "timeProgress")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.4f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedFraction)
                .align(Alignment.CenterStart)
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF4ADE80), Color(0xFF6366F1)))
                )
        )
    }
}

@Composable
fun NumberDisplay(base: Int, operator: Operator, delta: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "$base",
            fontSize = 96.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF2D2A4A)
        )
        Spacer(Modifier.height(16.dp))
        Surface(shape = RoundedCornerShape(24.dp), color = Color.White) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (operator == Operator.PLUS) "+" else "–",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )
                Spacer(Modifier.width(12.dp))
                Text("$delta", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF2D2A4A))
            }
        }
    }
}

@Composable
fun OptionsGrid(
    options: List<Int>,
    feedback: Feedback?,
    correctAnswer: Int,
    disabledOptions: Set<Int> = emptySet(),
    onOptionClick: (Int) -> Unit
) {
    var clicked by remember(options) { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        options.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowOptions.forEach { value ->
                    val isDisabled = disabledOptions.isNotEmpty() || value in disabledOptions
                    OptionCard(
                        value = value,
                        isSelected = clicked == value,
                        isCorrect = value == correctAnswer,
                        isDisabled = isDisabled,
                        feedback = feedback,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (!isDisabled) {
                                clicked = value
                                onOptionClick(value)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OptionCard(
    value: Int,
    isSelected: Boolean,
    isCorrect: Boolean,
    isDisabled: Boolean = false,
    feedback: Feedback?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        feedback == Feedback.CORRECT && isCorrect -> Color(0xFF22C55E)
        feedback == Feedback.WRONG && isSelected -> Color(0xFFEF4444)
        feedback == Feedback.WRONG && isCorrect -> Color(0xFF22C55E)
        else -> Color.White.copy(alpha = 0.85f)
    }
    val textColor = when {
        feedback == Feedback.CORRECT && isCorrect -> Color.White
        feedback == Feedback.WRONG && isSelected -> Color.White
        feedback == Feedback.WRONG && isCorrect -> Color.White
        else -> Color(0xFF2D2A4A)
    }
    val animatedBg by animateColorAsState(bgColor, label = "optionBg")

    Surface(
        modifier = modifier
            .height(90.dp)
            .clickable(enabled = !isDisabled, onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = animatedBg
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("$value", fontSize = 32.sp, fontWeight = FontWeight.Black, color = textColor)
        }
    }
}

@Composable
fun LevelAndQuestionBars(
    currentLevel: Int,
    currentQuestion: Int,
    wrongQuestions: Set<Int> = emptySet(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Levels section (2 rows of 10)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2D2A4A),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = "LEVEL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    (1..10).forEach { levelNum ->
                        LevelBox(
                            levelNum = levelNum,
                            currentLevel = currentLevel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    (11..20).forEach { levelNum ->
                        LevelBox(
                            levelNum = levelNum,
                            currentLevel = currentLevel,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Questions section (2 rows of 10)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF2D2A4A),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = "  Q  ",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    (1..10).forEach { qNum ->
                        QuestionBox(
                            qNum = qNum,
                            currentQuestion = currentQuestion,
                            isWrong = qNum in wrongQuestions,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    (11..20).forEach { qNum ->
                        QuestionBox(
                            qNum = qNum,
                            currentQuestion = currentQuestion,
                            isWrong = qNum in wrongQuestions,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LevelBox(levelNum: Int, currentLevel: Int, modifier: Modifier = Modifier) {
    val (bgColor, textColor) = when {
        levelNum < currentLevel -> Color(0xFF22C55E) to Color.White
        levelNum == currentLevel -> Color(0xFFF97316) to Color.White
        else -> Color.White.copy(alpha = 0.9f) to Color(0xFF64748B)
    }
    Box(
        modifier = modifier
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(
                width = if (levelNum == currentLevel) 1.5.dp else 1.dp,
                color = if (levelNum == currentLevel) Color(0xFFEA580C) else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$levelNum",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun QuestionBox(
    qNum: Int,
    currentQuestion: Int,
    isWrong: Boolean = false,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        qNum == currentQuestion -> Color(0xFFF97316) to Color.White
        isWrong -> Color(0xFFEF4444) to Color.White
        qNum < currentQuestion -> Color(0xFF22C55E) to Color.White
        else -> Color.White.copy(alpha = 0.9f) to Color(0xFF64748B)
    }
    Box(
        modifier = modifier
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(
                width = if (qNum == currentQuestion) 1.5.dp else 1.dp,
                color = if (qNum == currentQuestion) Color(0xFFEA580C) else if (isWrong) Color(0xFFDC2626) else Color(0xFFCBD5E1),
                shape = RoundedCornerShape(4.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$qNum",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
