package com.pp.Quickcalc.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pp.Quickcalc.ui.theme.DangerRed
import com.pp.Quickcalc.ui.theme.NeonEmerald
import com.pp.Quickcalc.ui.theme.SurfaceCardBorder
import com.pp.Quickcalc.ui.theme.WarningYellow

@Composable
fun GradientTimerBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 100),
        label = "timerProgress"
    )

    val barColor by animateColorAsState(
        targetValue = when {
            animatedProgress > 0.5f -> NeonEmerald
            animatedProgress > 0.25f -> WarningYellow
            else -> DangerRed
        },
        animationSpec = tween(durationMillis = 300),
        label = "timerColor"
    )

    val secondaryColor = when {
        animatedProgress > 0.5f -> Color(0xFF06B6D4)
        animatedProgress > 0.25f -> Color(0xFFF59E0B)
        else -> Color(0xFFB91C1C)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(SurfaceCardBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(7.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(barColor, secondaryColor)
                    )
                )
        )
    }
}
