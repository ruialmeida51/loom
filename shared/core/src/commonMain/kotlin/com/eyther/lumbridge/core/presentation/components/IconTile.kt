package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomSquircle

/**
 * The one icon background in the app: a tinted squircle. Pass [tint] for a semantic colour (the
 * tile then uses it at 12%); otherwise it uses the primary container.
 */
@Composable
fun IconTile(
    glyph: Glyph,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    tint: Color? = null,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .size(size)
                .background(color = tint?.copy(alpha = 0.12f) ?: colors.primaryContainer, shape = LoomSquircle),
    ) {
        LoomIcon(
            glyph = glyph,
            tint = tint ?: colors.onPrimaryContainer,
            decorative = true,
            modifier = Modifier.size(size * 0.5f),
        )
    }
}
