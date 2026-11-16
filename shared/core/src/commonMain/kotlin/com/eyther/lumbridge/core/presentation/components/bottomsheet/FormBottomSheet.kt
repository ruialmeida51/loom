package com.eyther.lumbridge.core.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/** Scroll the fields, keeping the primary action reachable with a keyboard or larger text. */
@Composable
fun FormBottomSheet(
    title: String,
    action: @Composable () -> Unit,
    dismissAction: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().testTag("form_sheet")) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier =
                Modifier.padding(horizontal = LoomSpacing.content, vertical = LoomSpacing.medium),
        )
        Column(
            modifier =
                Modifier.weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = LoomSpacing.content, vertical = LoomSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.content),
            content = content,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier.fillMaxWidth().padding(LoomSpacing.content),
            horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        ) {
            dismissAction()
            Column(modifier = Modifier.weight(1f)) { action() }
        }
    }
}
