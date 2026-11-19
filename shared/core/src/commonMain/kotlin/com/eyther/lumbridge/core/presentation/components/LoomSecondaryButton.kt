package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.eyther.lumbridge.core.presentation.components.button.DefaultLoomButton
import com.eyther.lumbridge.core.presentation.components.icon.Glyph

/** Secondary actions share the primary button's shape and touch height. */
@Composable
fun LoomSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    loading: Boolean = false,
    glyph: Glyph? = null,
    trailingGlyph: Glyph? = null,
    leadingContent: (@Composable () -> Unit)? = null,
) {
    DefaultLoomButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        glyph = glyph,
        trailingGlyph = trailingGlyph,
        leadingContent = leadingContent,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        outlineColor = MaterialTheme.colorScheme.outlineVariant,
    )
}
