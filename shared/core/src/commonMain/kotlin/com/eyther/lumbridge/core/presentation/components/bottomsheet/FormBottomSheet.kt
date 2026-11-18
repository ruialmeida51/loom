package com.eyther.lumbridge.core.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.action_cancel
import com.eyther.lumbridge.core.generated.resources.action_save
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import org.jetbrains.compose.resources.stringResource

/**
 * Scroll fields above full-width, stacked actions: primary first, optional Cancel below. Omit
 * onDismiss for immediate-action sheets.
 */
@Composable
fun FormBottomSheet(
    title: String,
    onAction: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    actionLabel: String = stringResource(Res.string.action_save),
    actionEnabled: Boolean = true,
    dismissLabel: String = stringResource(Res.string.action_cancel),
    busy: Boolean = false,
    actionModifier: Modifier = Modifier,
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
        Column(
            modifier = Modifier.fillMaxWidth().padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        ) {
            LoomButton(
                text = actionLabel,
                onClick = onAction,
                loading = busy,
                enabled = actionEnabled,
                modifier = actionModifier.fillMaxWidth(),
            )
            onDismiss?.let { dismiss ->
                LoomSecondaryButton(
                    text = dismissLabel,
                    onClick = dismiss,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
