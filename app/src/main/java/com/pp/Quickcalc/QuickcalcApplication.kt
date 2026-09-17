package com.pp.Quickcalc

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.pp.Quickcalc.ads.AppOpenAdManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuickcalcApplication : Application() {

    lateinit var appOpenAdManager: AppOpenAdManager
        private set

    companion object {
        lateinit var instance: QuickcalcApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        MobileAds.initialize(this) { }
        appOpenAdManager = AppOpenAdManager(this)
        appOpenAdManager.loadAd(this)
    }
}
