package com.pp.Quickcalc.ui.splash

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pp.Quickcalc.QuickcalcApplication
import com.pp.Quickcalc.ui.theme.DarkBackground
import com.pp.Quickcalc.ui.theme.NeonPurple
import com.pp.Quickcalc.ui.theme.NeonCyan
import com.pp.Quickcalc.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val appOpenAdManager = remember { QuickcalcApplication.instance.appOpenAdManager }

    val scaleAnim = remember { Animatable(0.7f) }

    DisposableEffect(Unit) {
        appOpenAdManager.isSplashActive = true
        onDispose {
            appOpenAdManager.isSplashActive = false
        }
    }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )

        var navigated = false
        fun proceed() {
            if (!navigated) {
                navigated = true
                onSplashFinished()
            }
        }

        val activity = context as? Activity
        if (activity != null && appOpenAdManager.isAdAvailable()) {
            appOpenAdManager.showAdIfAvailable(activity) {
                proceed()
            }
        } else {
            // Brief window to show branding & check for loaded ad before proceeding
            withTimeoutOrNull(2000L) {
                while (!appOpenAdManager.isAdAvailable()) {
                    delay(100L)
                }
            }
            if (activity != null && appOpenAdManager.isAdAvailable()) {
                appOpenAdManager.showAdIfAvailable(activity) {
                    proceed()
                }
            } else {
                proceed()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scaleAnim.value)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(NeonCyan, NeonPurple)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🧮",
                    fontSize = 48.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "QuickCalc",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Math IQ Game",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Train Your Brain • Quick Math Challenges",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = NeonCyan,
                strokeWidth = 3.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
