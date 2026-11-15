package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.ui_amount_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.generated.resources.ui_money_sent_by
import com.eyther.lumbridge.features.expenses.generated.resources.ui_received_by
import com.eyther.lumbridge.features.expenses.generated.resources.ui_record_a_repayment
import com.eyther.lumbridge.features.expenses.generated.resources.ui_record_repayment
import com.eyther.lumbridge.features.expenses.generated.resources.ui_this_records_a_payment_you_ve_already_made_or
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.presentation.components.MemberPicker
import org.jetbrains.compose.resources.stringResource

@Composable
fun SettlementBottomSheet(
    members: List<MemberDomain>,
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
        action = {
            LoomButton(
                stringResource(Res.string.ui_record_repayment),
                onSettle,
                loading = busy,
                modifier = Modifier.fillMaxWidth().testTag("record_repayment_submit"),
            )
        },
        dismissAction = {
            TextButton(onClick = { onShowSettle(false) }, enabled = !busy) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        MemberPicker(
            label = stringResource(Res.string.ui_money_sent_by),
            members = members,
            selectedId = from,
            onSelected = onFrom,
            enabled = !busy,
        )
        MemberPicker(
            label = stringResource(Res.string.ui_received_by),
            members = members,
            selectedId = to,
            onSelected = onTo,
            enabled = !busy,
        )
        OutlinedTextField(
            settleAmount,
            onSettleAmount,
            label = {
                Text(stringResource(Res.string.ui_amount_1_s, currency))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("repayment_amount"),
            enabled = !busy,
        )
        Text(
            stringResource(Res.string.ui_this_records_a_payment_you_ve_already_made_or),
            style = MaterialTheme.typography.bodySmall,
        )
        formError?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
    }
}
