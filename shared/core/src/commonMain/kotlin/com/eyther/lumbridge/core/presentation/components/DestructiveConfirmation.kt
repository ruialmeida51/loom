package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/**
 * Explicit disruptive-action confirmation; errors allow a deliberate retry and busy actions cannot
 * dismiss.
 */
@Composable
fun DestructiveConfirmation(
    confirmation: DestructiveConfirmationUi,
    busy: Boolean,
    confirmationTag: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    error: TextResource? = null,
) {
    AlertDialog(
        title = { Text(confirmation.title.getString()) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
                Text(confirmation.message.getString())
                error?.let {
                    Text(
                        text = it.getString(),
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                }
            }
        },
        onDismissRequest = { if (!busy) onDismiss() },
        confirmButton = {
            DialogActions(
                confirmText = confirmation.confirmText.getString(),
                dismissText = confirmation.cancelText.getString(),
                onConfirm = onConfirm,
                onDismiss = onDismiss,
                destructive = true,
                busy = busy,
                confirmModifier = Modifier.testTag(confirmationTag),
            )
        },
    )
}
