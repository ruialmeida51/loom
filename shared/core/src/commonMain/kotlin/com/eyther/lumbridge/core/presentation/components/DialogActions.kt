package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/** Equal-width actions for brief dialogs. Form sheets retain their vertically stacked footer. */
@Composable
fun DialogActions(
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmEnabled: Boolean = true,
    busy: Boolean = false,
    destructive: Boolean = false,
    confirmModifier: Modifier = Modifier,
) {
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = LoomSpacing.medium, vertical = LoomSpacing.small)
                .height(IntrinsicSize.Min)
                .testTag("dialog_actions"),
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
    ) {
        LoomSecondaryButton(
            text = dismissText,
            onClick = onDismiss,
            enabled = !busy,
            modifier = Modifier.weight(1f).fillMaxHeight().testTag("dialog_dismiss"),
        )
        if (destructive)
            DestructiveButton(
                text = confirmText,
                onClick = onConfirm,
                enabled = confirmEnabled,
                loading = busy,
                filled = true,
                modifier = confirmModifier.weight(1f).fillMaxHeight(),
            )
        else
            LoomButton(
                text = confirmText,
                onClick = onConfirm,
                enabled = confirmEnabled,
                loading = busy,
                modifier = confirmModifier.weight(1f).fillMaxHeight(),
            )
    }
}
