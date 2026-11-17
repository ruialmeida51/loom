package com.eyther.lumbridge.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

@Composable
fun LoomTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val darkMode = mode == ThemeMode.DARK || mode == ThemeMode.SYSTEM && isSystemInDarkTheme()
    ApplySystemTheme(darkMode)
    CompositionLocalProvider(
        LocalLoomSemanticColors provides
            if (darkMode) LoomDarkSemanticColors else LoomLightSemanticColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkMode) LoomDarkColorScheme else LoomLightColorScheme,
            typography = loomTypography(),
            content = content,
        )
    }
}
