package com.eyther.lumbridge.features.authentication.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.ic_eye_hidden
import com.eyther.lumbridge.features.authentication.generated.resources.ic_eye_open
import com.eyther.lumbridge.features.authentication.generated.resources.password_hide
import com.eyther.lumbridge.features.authentication.generated.resources.password_show
import com.eyther.lumbridge.features.authentication.generated.resources.ui_password
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Plannot's visibility-toggle pattern; visibility is transient and the value stays owned by the
 * form.
 */
@Composable
internal fun PasswordInput(
    value: String,
    onValueChanged: (String) -> Unit,
    enabled: Boolean,
    onSubmit: () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChanged,
        label = { Text(stringResource(Res.string.ui_password)) },
        modifier =
            Modifier.fillMaxWidth().testTag("auth_password").semantics {
                contentType = ContentType.Password
            },
        enabled = enabled,
        singleLine = true,
        visualTransformation =
            if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions =
            KeyboardOptions(
                keyboardType = KeyboardType.Password,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }, enabled = enabled) {
                Icon(
                    painter =
                        painterResource(
                            if (visible) Res.drawable.ic_eye_open else Res.drawable.ic_eye_hidden,
                        ),
                    contentDescription =
                        stringResource(
                            if (visible) Res.string.password_hide else Res.string.password_show,
                        ),
                )
            }
        },
    )
}
