package com.eyther.lumbridge.features.account.presentation.components.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.ui_edit_nickname
import com.eyther.lumbridge.features.account.generated.resources.ui_nickname_help
import com.eyther.lumbridge.features.account.generated.resources.ui_nickname_title
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
        onAction = onSave,
        onDismiss = onClose,
        actionEnabled = canSave,
        busy = busy,
        actionModifier = Modifier.testTag("save_nickname"),
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
