package com.pp.Quickcalc.ui.home

import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pp.Quickcalc.R
import com.pp.Quickcalc.model.Difficulty
import com.pp.Quickcalc.ui.components.AdBannerPlaceholder
import com.pp.Quickcalc.ui.components.rememberRotatingAngle
import com.pp.Quickcalc.ui.components.rotatingGradientBorder
import com.pp.Quickcalc.ui.theme.DarkBackground
import com.pp.Quickcalc.ui.theme.GameFontFamily
import com.pp.Quickcalc.ui.theme.NeonCyan
import com.pp.Quickcalc.ui.theme.NeonPurple
import com.pp.Quickcalc.ui.theme.SurfaceCard
import com.pp.Quickcalc.ui.theme.SurfaceCardBorder
import com.pp.Quickcalc.ui.theme.TextPrimary
import com.pp.Quickcalc.ui.theme.TextSecondary
import com.pp.Quickcalc.ui.theme.WarningYellow

val HomeBackgroundBrush = Brush.verticalGradient(
    colors = listOf(
        DarkBackground,
        Color(0xFF0F172A),
        DarkBackground
    )
)

@Composable
fun GradientOutlinedText(
    text: String,
    fontSize: TextUnit = 58.sp,
    fontFamily: FontFamily = GameFontFamily,
    letterSpacing: TextUnit = 2.sp
) {
    val angle = rememberRotatingAngle(durationMs = 3000)

    Box(contentAlignment = Alignment.Center) {
        // Outer Rotating Neon Gradient Stroke / Border around font letters
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            letterSpacing = letterSpacing,
            style = TextStyle(
                brush = ShaderBrush(
                    SweepGradient(
                        0f, 0f,
                        intArrayOf(
                            NeonCyan.toArgb(),
                            NeonPurple.toArgb(),
                            WarningYellow.toArgb(),
                            NeonCyan.toArgb()
                        ),
                        null
                    ).apply {
                        val matrix = Matrix()
                        matrix.postRotate(angle, 0f, 0f)
                        setLocalMatrix(matrix)
                    }
                ),
                drawStyle = Stroke(width = 10f, join = StrokeJoin.Round)
            )
        )

        // Inner Pitch Black Text Fill
        Text(
            text = text,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            letterSpacing = letterSpacing
        )
    }
}

@Composable
fun HomeScreen(
    onPlayClick: (Difficulty) -> Unit,
    onSettingsClick: () -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDifficulty by remember { mutableStateOf(Difficulty.EASY) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeBackgroundBrush)
            .padding(24.dp)
    ) {
        // Center Content Column
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Title in Russo One game font with Neon Gradient Border
            GradientOutlinedText(
                text = "QUICKCALC",
                fontSize = 46.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Mental Math Speed Challenge",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Difficulty Selector Title
            Text(
                text = "SELECT DIFFICULTY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Difficulty.values().forEach { diff ->
                    FilterChip(
                        selected = diff == selectedDifficulty,
                        onClick = { selectedDifficulty = diff },
                        label = {
                            Text(
                                text = diff.displayName.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan,
                            selectedLabelColor = DarkBackground,
                            containerColor = SurfaceCard,
                            labelColor = TextPrimary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = diff == selectedDifficulty,
                            selectedBorderColor = NeonCyan,
                            borderColor = SurfaceCardBorder
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Play Now Button with Rotating Neon Gradient Border
            Button(
                onClick = { onPlayClick(selectedDifficulty) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("play_now_button")
                    .rotatingGradientBorder(
                        borderWidth = 2.dp,
                        shape = RoundedCornerShape(29.dp)
                    ),
                shape = RoundedCornerShape(29.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "PLAY NOW ▶",
                    fontSize = 18.sp,
                    fontFamily = GameFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // How To Play Info Button with Rotating Neon Gradient Border
            Button(
                onClick = onInfoClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rotatingGradientBorder(
                        borderWidth = 2.dp,
                        shape = RoundedCornerShape(26.dp)
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "HOW TO PLAY ℹ",
                    fontSize = 15.sp,
                    fontFamily = GameFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Settings Button with Rotating Neon Gradient Border
            Button(
                onClick = onSettingsClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .rotatingGradientBorder(
                        borderWidth = 2.dp,
                        shape = RoundedCornerShape(26.dp)
                    ),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "SETTINGS ⚙",
                    fontSize = 15.sp,
                    fontFamily = GameFontFamily,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }

        // Bottom Ad Banner
        AdBannerPlaceholder(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        )
    }
}
