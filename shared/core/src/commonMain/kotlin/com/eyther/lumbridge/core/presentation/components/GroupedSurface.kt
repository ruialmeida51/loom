package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.theme.LoomColors

/** Cards lift off the ground with a soft shadow in light mode and a hairline in dark mode. */
internal val CardShadowElevation = 1.dp

/** One boundary for related rows; rows own their internal spacing and separators. */
@Composable
fun GroupedSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shadowElevation = if (LoomColors.cardShadow) CardShadowElevation else 0.dp,
        border = BorderStroke(width = 1.dp, color = LoomColors.cardBorder),
    ) {
        Column(content = content)
    }
}
