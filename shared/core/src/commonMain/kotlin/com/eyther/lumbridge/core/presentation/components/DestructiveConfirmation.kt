package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi

/** Reserve a dialog for a brief, explicit permanent-delete confirmation. */
@Composable
fun DestructiveConfirmation(
    confirmation: DestructiveConfirmationUi,
    busy: Boolean,
    confirmationTag: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(confirmation.title.getString()) },
        text = { Text(confirmation.message.getString()) },
        onDismissRequest = { if (!busy) onDismiss() },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !busy,
                colors =
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag(confirmationTag),
            ) {
                if (busy) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                }
                Text(confirmation.confirmText.getString())
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !busy) {
                Text(confirmation.cancelText.getString())
            }
        },
    )
}
