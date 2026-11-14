package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.ui_add_a_shared_expense
import com.eyther.lumbridge.features.expenses.generated.resources.ui_amount_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.generated.resources.ui_category
import com.eyther.lumbridge.features.expenses.generated.resources.ui_payment_date
import com.eyther.lumbridge.features.expenses.generated.resources.ui_repeat_explanation
import com.eyther.lumbridge.features.expenses.generated.resources.ui_repeat_this_bill_monthly
import com.eyther.lumbridge.features.expenses.generated.resources.ui_save_expense
import com.eyther.lumbridge.features.expenses.generated.resources.ui_split_with
import com.eyther.lumbridge.features.expenses.generated.resources.ui_what_was_it_for
import com.eyther.lumbridge.features.expenses.generated.resources.ui_who_paid
import com.eyther.lumbridge.features.expenses.presentation.mapper.expenseOptionLabel
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.presentation.components.MemberPicker
import org.jetbrains.compose.resources.stringResource

@Composable
fun SharedExpenseBottomSheet(
    members: List<MemberDomain>,
    title: String,
    amount: String,
    occurredOn: String,
    paidBy: String,
    selectedCategory: String,
    selected: Set<String>,
    splitMode: String,
    custom: Map<String, String>,
    recurring: Boolean,
    busy: Boolean,
    formError: TextResource?,
    context: HouseholdContextDomain,
    onTitle: (String) -> Unit,
    onAmount: (String) -> Unit,
    onOccurredOn: (String) -> Unit,
    onPayer: (String) -> Unit,
    onCategory: (String) -> Unit,
    onSelected: (String) -> Unit,
    onSplitMode: (String) -> Unit,
    onCustom: (String, String) -> Unit,
    onRecurring: (Boolean) -> Unit,
    onShowAdd: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onDismiss: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_add_a_shared_expense),
        action = {
            LoomButton(stringResource(Res.string.ui_save_expense), onAdd, loading = busy)
        },
        dismissAction = {
            TextButton(onClick = { onShowAdd(false) }, enabled = !busy) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        OutlinedTextField(
            title,
            onTitle,
            label = { Text(stringResource(Res.string.ui_what_was_it_for)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        OutlinedTextField(
            amount,
            onAmount,
            label = {
                Text(stringResource(Res.string.ui_amount_1_s, context.currency))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        OutlinedTextField(
            occurredOn,
            onOccurredOn,
            label = { Text(stringResource(Res.string.ui_payment_date)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        MemberPicker(
            label = stringResource(Res.string.ui_who_paid),
            members = members,
            selectedId = paidBy,
            onSelected = onPayer,
            enabled = !busy,
        )
        Text(
            stringResource(Res.string.ui_split_with),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Equal", "Amounts", "Percent").forEach { mode ->
                FilterChip(
                    splitMode == mode,
                    { onSplitMode(mode) },
                    label = { Text(expenseOptionLabel(mode)) },
                    enabled = !busy,
                )
            }
        }
        members.forEach { member ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    member.id in selected,
                    { onSelected(member.id) },
                    enabled = !busy,
                )
                Text(member.name, Modifier.weight(1f))
                if (splitMode != "Equal" && member.id in selected)
                    OutlinedTextField(
                        custom[member.id].orEmpty(),
                        { onCustom(member.id, it) },
                        modifier = Modifier.width(90.dp),
                        label = {
                            Text(if (splitMode == "Percent") "%" else context.currency)
                        },
                        singleLine = true,
                        enabled = !busy,
                    )
            }
        }
        Text(
            stringResource(Res.string.ui_category),
            style = MaterialTheme.typography.labelLarge,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Groceries", "Bills", "Food & drink", "Home", "Other").forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategory(category) },
                    label = { Text(expenseOptionLabel(category)) },
                    enabled = !busy,
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                recurring,
                onRecurring,
                modifier = Modifier.testTag("expense_recurring"),
                enabled = !busy,
            )
            Text(stringResource(Res.string.ui_repeat_this_bill_monthly))
        }
        if (recurring)
            Text(
                stringResource(Res.string.ui_repeat_explanation),
                style = MaterialTheme.typography.bodySmall,
            )
        formError?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
    }
}
