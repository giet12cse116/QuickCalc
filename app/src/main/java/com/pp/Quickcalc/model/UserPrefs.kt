package com.pp.Quickcalc.model

data class UserPrefs(
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val darkTheme: Boolean = false,
    val hasShownRatePrompt: Boolean = false
)
