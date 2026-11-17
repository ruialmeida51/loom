package com.eyther.lumbridge.features.account.presentation.components.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.ui_cancel
import com.eyther.lumbridge.features.account.generated.resources.ui_edit_nickname
import com.eyther.lumbridge.features.account.generated.resources.ui_nickname_help
import com.eyther.lumbridge.features.account.generated.resources.ui_nickname_title
import com.eyther.lumbridge.features.account.generated.resources.ui_save_nickname
import org.jetbrains.compose.resources.stringResource

@Composable
fun NicknameBottomSheet(
    nickname: String,
    error: TextResource?,
    busy: Boolean,
    canSave: Boolean,
    onChanged: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_edit_nickname),
        action = {
            LoomButton(
                text = stringResource(Res.string.ui_save_nickname),
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                loading = busy,
                enabled = canSave,
            )
        },
        dismissAction = {
            OutlinedButton(onClick = onClose, enabled = !busy) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        Text(
            text = stringResource(Res.string.ui_nickname_help),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = nickname,
            onValueChange = onChanged,
            label = { Text(stringResource(Res.string.ui_nickname_title)) },
            modifier = Modifier.fillMaxWidth().testTag("nickname_input"),
            singleLine = true,
            enabled = !busy,
            isError = error != null,
            supportingText = error?.let { message -> { Text(message.getString()) } },
        )
    }
}
