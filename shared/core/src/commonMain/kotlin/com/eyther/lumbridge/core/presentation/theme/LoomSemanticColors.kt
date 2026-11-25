package com.eyther.lumbridge.core.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic roles beyond Material's: owed/positive feedback, card edges, snackbars and the stable
 * colours that tell household members apart. Text must still explain any state colour shows.
 */
@Immutable
internal data class LoomSemanticColors(
    val positive: Color,
    val positiveContainer: Color,
    val onPositiveContainer: Color,
    val cardBorder: Color,
    val cardShadow: Boolean,
    val snackbar: Color,
    val onSnackbar: Color,
    val snackbarAction: Color,
    val members: List<Color>,
    val onMember: Color,
)

internal val LoomLightSemanticColors =
    LoomSemanticColors(
        positive = Color(0xFF1E6B57),
        positiveContainer = Color(0xFFE2F0EB),
        onPositiveContainer = Color(0xFF164F40),
        cardBorder = Color.Transparent,
        cardShadow = true,
        snackbar = Color(0xFF1B1A17),
        onSnackbar = Color(0xFFF5F3EE),
        snackbarAction = Color(0xFF9FE3CC),
        members =
            listOf(
                Color(0xFF1E6B57),
                Color(0xFFB4651E),
                Color(0xFF6A58B0),
                Color(0xFF2E6FA8),
                Color(0xFFA33D6E),
                Color(0xFF5E6B1F),
            ),
        onMember = Color(0xFFFFFFFF),
    )
internal val LoomDarkSemanticColors =
    LoomSemanticColors(
        positive = Color(0xFF7FDABD),
        positiveContainer = Color(0xFF1E4D40),
        onPositiveContainer = Color(0xFFADF0D8),
        cardBorder = Color.Transparent,
        cardShadow = false,
        snackbar = Color(0xFF3A3936),
        onSnackbar = Color(0xFFEFECE6),
        snackbarAction = Color(0xFF7FDABD),
        members =
            listOf(
                Color(0xFF7FDABD),
                Color(0xFFF0B07A),
                Color(0xFFB8ACF0),
                Color(0xFF94C1EE),
                Color(0xFFEDA3C8),
                Color(0xFFC9D684),
            ),
        onMember = Color(0xFF141413),
    )
internal val LocalLoomSemanticColors = staticCompositionLocalOf { LoomLightSemanticColors }

object LoomColors {
    val positive: Color
        @Composable get() = LocalLoomSemanticColors.current.positive

    val positiveContainer: Color
        @Composable get() = LocalLoomSemanticColors.current.positiveContainer

    val onPositiveContainer: Color
        @Composable get() = LocalLoomSemanticColors.current.onPositiveContainer

    val cardBorder: Color
        @Composable get() = LocalLoomSemanticColors.current.cardBorder

    val cardShadow: Boolean
        @Composable get() = LocalLoomSemanticColors.current.cardShadow

    val snackbar: Color
        @Composable get() = LocalLoomSemanticColors.current.snackbar

    val onSnackbar: Color
        @Composable get() = LocalLoomSemanticColors.current.onSnackbar

    val snackbarAction: Color
        @Composable get() = LocalLoomSemanticColors.current.snackbarAction

    val onMember: Color
        @Composable get() = LocalLoomSemanticColors.current.onMember

    /** A stable colour per member id, so the same person looks the same on every screen. */
    @Composable
    fun member(memberId: String): Color {
        val palette = LocalLoomSemanticColors.current.members
        return palette[memberColorIndex(memberId = memberId, size = palette.size)]
    }
}

internal fun memberColorIndex(memberId: String, size: Int): Int =
    memberId.fold(0) { hash, char -> (hash * 31 + char.code) and 0x7FFFFFFF } % size
