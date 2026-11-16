package com.eyther.lumbridge.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

private val typography = Typography()

@Composable
fun LoomTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val darkMode = mode == ThemeMode.DARK || mode == ThemeMode.SYSTEM && isSystemInDarkTheme()
    ApplySystemTheme(darkMode)
    MaterialTheme(
        colorScheme = if (darkMode) LoomDarkColorScheme else LoomLightColorScheme,
        typography = typography,
        content = content,
    )
}
