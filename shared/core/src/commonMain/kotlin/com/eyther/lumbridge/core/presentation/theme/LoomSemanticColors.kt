package com.eyther.lumbridge.core.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Semantic feedback roles supplement Material's error roles; text must still explain the state. */
@Immutable
internal data class LoomSemanticColors(
    val positive: Color,
    val positiveContainer: Color,
    val onPositiveContainer: Color,
)

internal val LoomLightSemanticColors =
    LoomSemanticColors(
        positive = Color(0xFF21643E),
        positiveContainer = Color(0xFFD6F2DF),
        onPositiveContainer = Color(0xFF102D1D),
    )
internal val LoomDarkSemanticColors =
    LoomSemanticColors(
        positive = Color(0xFF8CD7AA),
        positiveContainer = Color(0xFF173D29),
        onPositiveContainer = Color(0xFFD6F2DF),
    )
internal val LocalLoomSemanticColors = staticCompositionLocalOf { LoomLightSemanticColors }

object LoomColors {
    val positive: Color
        @Composable get() = LocalLoomSemanticColors.current.positive

    val positiveContainer: Color
        @Composable get() = LocalLoomSemanticColors.current.positiveContainer

    val onPositiveContainer: Color
        @Composable get() = LocalLoomSemanticColors.current.onPositiveContainer
}
