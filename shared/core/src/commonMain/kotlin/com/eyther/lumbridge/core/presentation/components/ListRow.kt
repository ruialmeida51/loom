package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/**
 * Repeated content stays in a list, with one quiet separator instead of a card around every item.
 */
@Composable
fun ListRow(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().padding(top = LoomSpacing.small),
        verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
    ) {
        content()
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
