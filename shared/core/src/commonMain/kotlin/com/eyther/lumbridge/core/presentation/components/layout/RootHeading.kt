package com.eyther.lumbridge.core.presentation.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val RootHeadingMinimumHeight = 48.dp // Align static headings with accessible selectors.

/** Static root headings share the height and baseline of the shopping/account selectors. */
@Composable
fun RootHeading(title: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = RootHeadingMinimumHeight),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
    }
}
