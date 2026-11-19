package com.eyther.lumbridge.core.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.eyther.lumbridge.core.presentation.components.button.DefaultLoomButton

/** Destructive secondary action with a visible boundary and request progress. */
@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    filled: Boolean = false,
) {
    DefaultLoomButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        loading = loading,
        containerColor = if (filled) MaterialTheme.colorScheme.error else Color.Transparent,
        contentColor =
            if (filled) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.error,
        outlineColor = if (filled) null else MaterialTheme.colorScheme.error,
    )
}
