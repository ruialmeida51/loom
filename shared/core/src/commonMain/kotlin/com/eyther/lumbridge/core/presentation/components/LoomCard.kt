package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/**
 * The Loom card surface: tonal container, hairline border and, in light mode, a soft shadow.
 * Pass [contentPadding] = `PaddingValues()` for edge-to-edge content such as a picture, and pad
 * the inner sections yourself.
 */
@Composable
fun LoomCard(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    contentPadding: PaddingValues = PaddingValues(LoomSpacing.content),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(LoomSpacing.medium),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = if (LoomColors.cardShadow) CardShadowElevation else 0.dp,
        border = BorderStroke(width = 1.dp, color = LoomColors.cardBorder),
    ) {
        Column(
            Modifier.padding(contentPadding),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}

/** Clickable counterpart with the same surface, padding and shape as grouped Loom content. */
@Composable
fun LoomCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
    contentPadding: PaddingValues = PaddingValues(LoomSpacing.content),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(LoomSpacing.medium),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = if (LoomColors.cardShadow) CardShadowElevation else 0.dp,
        border = BorderStroke(width = 1.dp, color = LoomColors.cardBorder),
    ) {
        Column(
            Modifier.padding(contentPadding),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
