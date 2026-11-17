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
import androidx.compose.ui.unit.Dp
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/**
 * Scroll fields above fixed actions. verticalActions places the full-width primary action before
 * Cancel.
 */
@Composable
fun FormBottomSheet(
    title: String,
    action: @Composable () -> Unit,
    dismissAction: @Composable () -> Unit,
    verticalActions: Boolean = false,
    contentSpacing: Dp = LoomSpacing.content,
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
            verticalArrangement = Arrangement.spacedBy(contentSpacing),
            content = content,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        if (verticalActions) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(LoomSpacing.content),
                verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
            ) {
                action()
                dismissAction()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(LoomSpacing.content),
                horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
            ) {
                dismissAction()
                Column(modifier = Modifier.weight(1f)) { action() }
            }
        }
    }
}
