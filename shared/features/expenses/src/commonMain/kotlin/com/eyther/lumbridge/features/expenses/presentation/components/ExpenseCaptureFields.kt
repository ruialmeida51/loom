package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.form_amount_label
import com.eyther.lumbridge.features.expenses.generated.resources.form_description
import org.jetbrains.compose.resources.stringResource

/** Draft capture is independent of reference-data loading; IME Next moves into the description. */
@Composable
internal fun ExpenseCaptureFields(
    amount: String,
    title: String,
    currency: String?,
    enabled: Boolean,
    onAmount: (String) -> Unit,
    onTitle: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = amount,
        onValueChange = onAmount,
        label = { Text(stringResource(Res.string.form_amount_label)) },
        suffix = { currency?.let { Text(it) } },
        textStyle = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"),
        keyboardOptions =
            KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Next) }),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag("expense_amount"),
    )
    OutlinedTextField(
        value = title,
        onValueChange = onTitle,
        label = { Text(stringResource(Res.string.form_description)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        singleLine = true,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag("expense_title"),
    )
}
