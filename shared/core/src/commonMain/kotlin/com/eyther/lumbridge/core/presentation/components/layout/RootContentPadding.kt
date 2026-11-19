package com.eyther.lumbridge.core.presentation.components.layout

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/**
 * Root content rhythm. Hosts own system insets; children may reserve FAB clearance or edge-to-edge
 * rows.
 */
fun rootContentPadding(
    horizontal: Dp = LoomSpacing.content,
    bottom: Dp = LoomSpacing.content,
): PaddingValues =
    PaddingValues(
        start = horizontal,
        top = LoomSpacing.content,
        end = horizontal,
        bottom = bottom,
    )
