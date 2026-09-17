package com.pp.Quickcalc

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.pp.Quickcalc.model.UserPrefs
import com.pp.Quickcalc.navigation.NumflowNavGraph
import com.pp.Quickcalc.ui.settings.SettingsViewModel
import com.pp.Quickcalc.ui.theme.NumflowTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val appBgColor = Color.parseColor("#0F172A")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(appBgColor),
            navigationBarStyle = SystemBarStyle.dark(appBgColor)
        )
        @Suppress("DEPRECATION")
        window.statusBarColor = appBgColor
        @Suppress("DEPRECATION")
        window.navigationBarColor = appBgColor

        setContent {
            NumFlowApp()
        }
    }
}

@Composable
fun NumFlowApp() {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val prefs by settingsViewModel.prefs.collectAsState(initial = UserPrefs())

    NumflowTheme(darkTheme = prefs.darkTheme) {
        val navController = rememberNavController()
        NumflowNavGraph(navController = navController)
    }
}
