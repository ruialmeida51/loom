package com.eyther.lumbridge.core.presentation.theme

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun ApplySystemTheme(dark: Boolean) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window
        if (window != null)
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !dark
                isAppearanceLightNavigationBars = !dark
            }
    }
}
