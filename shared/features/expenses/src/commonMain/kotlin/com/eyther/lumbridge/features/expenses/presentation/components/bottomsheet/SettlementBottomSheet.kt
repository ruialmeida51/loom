package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.ui_amount_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.generated.resources.ui_money_sent_by
import com.eyther.lumbridge.features.expenses.generated.resources.ui_received_by
import com.eyther.lumbridge.features.expenses.generated.resources.ui_record_a_repayment
import com.eyther.lumbridge.features.expenses.generated.resources.ui_record_repayment
import com.eyther.lumbridge.features.expenses.generated.resources.ui_this_records_a_payment_you_ve_already_made_or
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.presentation.components.MemberPickerRow
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettlementBottomSheet(
    members: List<MemberDomain>,
    currentUserId: String,
    from: String,
    to: String,
    settleAmount: String,
    busy: Boolean,
    formError: TextResource?,
    currency: String,
    onShowSettle: (Boolean) -> Unit,
    onFrom: (String) -> Unit,
    onTo: (String) -> Unit,
    onSettleAmount: (String) -> Unit,
    onSettle: () -> Unit,
    onDismiss: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_record_a_repayment),
        verticalActions = true,
        action = {
            LoomButton(
                stringResource(Res.string.ui_record_repayment),
                onSettle,
                loading = busy,
                modifier = Modifier.fillMaxWidth().testTag("record_repayment_submit"),
            )
        },
        dismissAction = {
            OutlinedButton(
                onClick = { onShowSettle(false) },
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        OutlinedTextField(
            settleAmount,
            onSettleAmount,
            label = {
                Text(stringResource(Res.string.ui_amount_1_s, currency))
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().testTag("repayment_amount"),
            enabled = !busy,
        )
        GroupedSurface {
            MemberPickerRow(
                label = stringResource(Res.string.ui_money_sent_by),
                members = members,
                selectedId = from,
                currentUserId = currentUserId,
                onSelected = onFrom,
                enabled = !busy,
                emphasisColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.testTag("payment_sender"),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            MemberPickerRow(
                label = stringResource(Res.string.ui_received_by),
                members = members,
                selectedId = to,
                currentUserId = currentUserId,
                onSelected = onTo,
                enabled = !busy,
                emphasisColor = LoomColors.positive,
                modifier = Modifier.testTag("payment_recipient"),
            )
        }
        Text(
            stringResource(Res.string.ui_this_records_a_payment_you_ve_already_made_or),
            style = MaterialTheme.typography.bodySmall,
        )
        formError?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
    }
}
