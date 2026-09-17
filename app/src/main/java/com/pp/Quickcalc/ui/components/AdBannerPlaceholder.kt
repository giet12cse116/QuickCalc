package com.pp.Quickcalc.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

const val BANNER_AD_UNIT_ID = "ca-app-pub-5520583411219682/4801226450"

@Composable
fun AdBannerPlaceholder(modifier: Modifier = Modifier) {
    AdBannerSlot(modifier = modifier)
}

@Composable
fun AdBannerSlot(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adView = remember(context) {
        AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = BANNER_AD_UNIT_ID
        }
    }

    DisposableEffect(adView) {
        try {
            adView.loadAd(AdRequest.Builder().build())
        } catch (e: Exception) {
            // Graceful fallback
        }
        onDispose {
            try {
                adView.destroy()
            } catch (e: Exception) {
                // Graceful cleanup
            }
        }
    }

    AndroidView(modifier = modifier.fillMaxWidth(), factory = { adView })
}
