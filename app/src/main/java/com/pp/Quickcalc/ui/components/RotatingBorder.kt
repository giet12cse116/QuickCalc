package com.pp.Quickcalc.ui.components

import android.graphics.Matrix
import android.graphics.SweepGradient
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pp.Quickcalc.ui.theme.NeonCyan
import com.pp.Quickcalc.ui.theme.NeonPurple
import com.pp.Quickcalc.ui.theme.WarningYellow

@Composable
fun rememberRotatingAngleState(durationMs: Int = 3000): State<Float> {
    val transition = rememberInfiniteTransition(label = "globalBorderRotation")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
}

@Composable
fun rememberRotatingAngle(durationMs: Int = 3000): Float {
    return rememberRotatingAngleState(durationMs).value
}

fun Modifier.drawRotatingGradientBorder(
    angleState: State<Float>,
    borderWidth: Dp = 2.dp,
    shape: Shape,
    colors: List<Color> = listOf(NeonCyan, NeonPurple, WarningYellow, NeonCyan)
): Modifier = this.drawWithCache {
    val strokePx = borderWidth.toPx()
    val outline = shape.createOutline(size, layoutDirection, this)
    val path = Path().apply { addOutline(outline) }
    val colorInts = colors.map { it.toArgb() }.toIntArray()
    val matrix = Matrix()
    val cx = size.width / 2f
    val cy = size.height / 2f
    val shader = if (size.width > 0 && size.height > 0) {
        SweepGradient(cx, cy, colorInts, null)
    } else null

    onDrawWithContent {
        drawContent()
        if (shader != null && size.width > 0 && size.height > 0) {
            matrix.reset()
            matrix.postRotate(angleState.value, cx, cy)
            shader.setLocalMatrix(matrix)

            drawPath(
                path = path,
                brush = ShaderBrush(shader),
                style = Stroke(width = strokePx)
            )
        }
    }
}

fun Modifier.rotatingGradientBorder(
    borderWidth: Dp = 2.dp,
    shape: Shape,
    colors: List<Color> = listOf(NeonCyan, NeonPurple, WarningYellow, NeonCyan),
    angleProvider: (() -> Float)? = null,
    durationMs: Int = 3000
): Modifier = composed {
    val defaultAngle = if (angleProvider == null) rememberRotatingAngle(durationMs) else 0f
    val colorInts = remember(colors) { colors.map { it.toArgb() }.toIntArray() }
    val matrix = remember { Matrix() }

    this.drawWithCache {
        val strokePx = borderWidth.toPx()
        val outline = shape.createOutline(size, layoutDirection, this)
        val path = Path().apply { addOutline(outline) }
        val cx = size.width / 2f
        val cy = size.height / 2f
        val shader = if (size.width > 0 && size.height > 0) {
            SweepGradient(cx, cy, colorInts, null)
        } else null

        onDrawWithContent {
            drawContent()
            if (shader != null && size.width > 0 && size.height > 0) {
                val currentAngle = angleProvider?.invoke() ?: defaultAngle
                matrix.reset()
                matrix.postRotate(currentAngle, cx, cy)
                shader.setLocalMatrix(matrix)

                drawPath(
                    path = path,
                    brush = ShaderBrush(shader),
                    style = Stroke(width = strokePx)
                )
            }
        }
    }
}
