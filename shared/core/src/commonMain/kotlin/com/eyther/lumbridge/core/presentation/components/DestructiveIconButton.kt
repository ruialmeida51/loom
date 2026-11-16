package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon

/** Compact destructive action for list rows; callers name the affected item for accessibility. */
@Composable
fun DestructiveIconButton(
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    OutlinedIconButton(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.size(48.dp).semantics { contentDescription = description },
        colors =
            IconButtonDefaults.outlinedIconButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
        else
            LoomIcon(
                glyph = Glyph.DELETE,
                modifier = Modifier.clearAndSetSemantics {},
                tint = MaterialTheme.colorScheme.error,
            )
    }
}
