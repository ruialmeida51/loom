package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
            ) {
                LoomSecondaryButton(
                    text = confirmation.cancelText.getString(),
                    onClick = onDismiss,
                    enabled = !busy,
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
                Button(
                    onClick = onConfirm,
                    enabled = !busy,
                    shape = MaterialTheme.shapes.medium,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ),
                    modifier =
                        Modifier.weight(1f)
                            .fillMaxHeight()
                            .heightIn(min = 50.dp)
                            .testTag(confirmationTag),
                ) {
                    if (busy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError,
                        )
                        Spacer(Modifier.width(LoomSpacing.small))
                    }
                    Text(confirmation.confirmText.getString(), textAlign = TextAlign.Center)
                }
            }
        },
    )
}
