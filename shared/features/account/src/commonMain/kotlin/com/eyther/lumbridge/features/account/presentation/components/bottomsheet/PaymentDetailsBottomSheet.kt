package com.eyther.lumbridge.features.account.presentation.components.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.account.generated.resources.Res
import com.eyther.lumbridge.features.account.generated.resources.payment_details_help
import com.eyther.lumbridge.features.account.generated.resources.payment_details_mbway
import com.eyther.lumbridge.features.account.generated.resources.payment_details_mbway_hint
import com.eyther.lumbridge.features.account.generated.resources.payment_details_paypal
import com.eyther.lumbridge.features.account.generated.resources.payment_details_paypal_hint
import com.eyther.lumbridge.features.account.generated.resources.payment_details_revolut
import com.eyther.lumbridge.features.account.generated.resources.payment_details_revolut_hint
import com.eyther.lumbridge.features.account.generated.resources.payment_details_title
import org.jetbrains.compose.resources.stringResource

/** How housemates can pay this person back. Every field is optional. */
@Composable
fun PaymentDetailsBottomSheet(
    mbwayPhone: String,
    revolutUsername: String,
    paypalUsername: String,
    error: TextResource?,
    busy: Boolean,
    onMbwayPhone: (String) -> Unit,
    onRevolutUsername: (String) -> Unit,
    onPaypalUsername: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.payment_details_title),
        onAction = onSave,
        onDismiss = onClose,
        busy = busy,
        actionModifier = Modifier.testTag("save_payment_details"),
    ) {
        Text(
            text = stringResource(Res.string.payment_details_help),
            style = MaterialTheme.typography.bodyMedium,
        )
        PaymentField(
            value = mbwayPhone,
            label = stringResource(Res.string.payment_details_mbway),
            hint = stringResource(Res.string.payment_details_mbway_hint),
            keyboardType = KeyboardType.Phone,
            enabled = !busy,
            onValue = onMbwayPhone,
            tag = "payment_mbway",
        )
        PaymentField(
            value = revolutUsername,
            label = stringResource(Res.string.payment_details_revolut),
            hint = stringResource(Res.string.payment_details_revolut_hint),
            keyboardType = KeyboardType.Ascii,
            enabled = !busy,
            onValue = onRevolutUsername,
            tag = "payment_revolut",
        )
        PaymentField(
            value = paypalUsername,
            label = stringResource(Res.string.payment_details_paypal),
            hint = stringResource(Res.string.payment_details_paypal_hint),
            keyboardType = KeyboardType.Ascii,
            enabled = !busy,
            onValue = onPaypalUsername,
            tag = "payment_paypal",
        )
        error?.let { ErrorNotice(message = it, onDismiss = null) }
    }
}

@Composable
private fun PaymentField(
    value: String,
    label: String,
    hint: String,
    keyboardType: KeyboardType,
    enabled: Boolean,
    onValue: (String) -> Unit,
    tag: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        label = { Text(label) },
        supportingText = { Text(hint) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth().testTag(tag),
    )
}
