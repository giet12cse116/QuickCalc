package com.pp.Quickcalc.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class RewardedAdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    companion object {
        const val REWARDED_AD_UNIT_ID = "ca-app-pub-5520583411219682/4037230112"
    }

    init {
        load()
    }

    fun load(adUnitId: String = REWARDED_AD_UNIT_ID) {
        if (isLoading || rewardedAd != null) return

        isLoading = true
        try {
            RewardedAd.load(
                context,
                adUnitId,
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        isLoading = false
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        rewardedAd = null
                        isLoading = false
                    }
                }
            )
        } catch (e: Exception) {
            rewardedAd = null
            isLoading = false
        }
    }

    fun show(activity: Activity?, onReward: () -> Unit) {
        val ad = rewardedAd
        if (activity != null && ad != null) {
            var rewardEarned = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    load()
                    if (rewardEarned) {
                        onReward()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    rewardedAd = null
                    load()
                    onReward()
                }
            }

            ad.show(activity) { _ ->
                rewardEarned = true
            }
        } else {
            load()
            onReward()
        }
    }
}
