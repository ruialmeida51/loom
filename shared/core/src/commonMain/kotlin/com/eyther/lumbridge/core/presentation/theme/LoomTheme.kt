package com.eyther.lumbridge.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val LoomGold = Color(0xFFD3AC44)
val LoomSage = Color(0xFF60745C)

private val light =
    lightColorScheme(
        primary = Color(0xFF725918),
        onPrimary = Color.White,
        primaryContainer = LoomGold,
        onPrimaryContainer = Color(0xFF2E260F),
        secondary = LoomSage,
        secondaryContainer = Color(0xFFE8EDDF),
        onSecondaryContainer = Color(0xFF34462F),
        background = Color(0xFFFAF8F2),
        onBackground = Color(0xFF282C25),
        surface = Color(0xFFFFFEFA),
        onSurface = Color(0xFF282C25),
        surfaceVariant = Color(0xFFF1EEE5),
        onSurfaceVariant = Color(0xFF74766B),
        outline = Color(0xFFDAD8CE),
        outlineVariant = Color(0xFFE9E5D9),
    )
private val dark =
    darkColorScheme(
        primary = LoomGold,
        onPrimary = Color(0xFF2E260F),
        primaryContainer = Color(0xFF51401C),
        onPrimaryContainer = Color(0xFFF2D887),
        secondary = Color(0xFFADBF9D),
        secondaryContainer = Color(0xFF303D2C),
        onSecondaryContainer = Color(0xFFD5E3CA),
        background = Color(0xFF181B17),
        onBackground = Color(0xFFF2F1E9),
        surface = Color(0xFF22261F),
        onSurface = Color(0xFFF2F1E9),
        surfaceVariant = Color(0xFF2D3129),
        onSurfaceVariant = Color(0xFFB5B8AB),
        outline = Color(0xFF505548),
        outlineVariant = Color(0xFF363C31),
    )

@Composable
fun LoomTheme(mode: ThemeMode, content: @Composable () -> Unit) {
    val darkMode = mode == ThemeMode.DARK || mode == ThemeMode.SYSTEM && isSystemInDarkTheme()
    ApplySystemTheme(darkMode)
    MaterialTheme(
        colorScheme = if (darkMode) dark else light,
        typography =
            Typography(
                displaySmall =
                    TextStyle(fontFamily = FontFamily.Serif, fontSize = 36.sp, lineHeight = 41.sp),
                headlineLarge =
                    TextStyle(fontFamily = FontFamily.Serif, fontSize = 32.sp, lineHeight = 38.sp),
                headlineMedium =
                    TextStyle(fontFamily = FontFamily.Serif, fontSize = 26.sp, lineHeight = 32.sp),
                titleLarge =
                    TextStyle(fontSize = 20.sp, lineHeight = 27.sp, fontWeight = FontWeight.Medium),
                titleMedium =
                    TextStyle(fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.Medium),
                bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 25.sp),
                bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
                labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
            ),
        content = content,
    )
}
