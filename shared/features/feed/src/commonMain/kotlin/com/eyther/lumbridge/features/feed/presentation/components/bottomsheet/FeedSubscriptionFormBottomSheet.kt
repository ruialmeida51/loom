package com.eyther.lumbridge.features.feed.presentation.components.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.feed.generated.resources.Res
import com.eyther.lumbridge.features.feed.generated.resources.feed_add
import com.eyther.lumbridge.features.feed.generated.resources.feed_edit
import com.eyther.lumbridge.features.feed.generated.resources.feed_save
import com.eyther.lumbridge.features.feed.generated.resources.ui_cancel
import com.eyther.lumbridge.features.feed.generated.resources.ui_feed_url_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun FeedSubscriptionFormBottomSheet(
    url: String,
    editing: Boolean,
    busy: Boolean,
    canSave: Boolean,
    error: TextResource?,
    onUrl: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
    onDismissError: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(if (editing) Res.string.feed_edit else Res.string.feed_add),
        action = {
            LoomButton(
                text = stringResource(Res.string.feed_save),
                onClick = onSave,
                enabled = canSave,
                loading = busy,
            )
        },
        dismissAction = {
            OutlinedButton(onClick = onClose, enabled = !busy) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = onUrl,
            label = { Text(stringResource(Res.string.ui_feed_url_hint)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        error?.let { ErrorNotice(message = it, onDismiss = onDismissError) }
    }
}
