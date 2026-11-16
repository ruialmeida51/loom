package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.extensions.shimmer

/** Plannot's two-line row skeleton, matching the list rather than showing a blank card. */
@Composable
fun LoadingRow(modifier: Modifier = Modifier, leadingSize: Dp = 32.dp) {
    Row(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics {}.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(leadingSize).shimmer(true))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LoadingLine(widthFraction = .6f)
            LoadingLine(widthFraction = .85f, style = MaterialTheme.typography.bodySmall)
        }
    }
}
