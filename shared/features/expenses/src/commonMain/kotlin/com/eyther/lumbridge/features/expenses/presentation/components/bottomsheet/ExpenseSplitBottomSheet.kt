package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.SelectorButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.form_split
import com.eyther.lumbridge.features.expenses.generated.resources.split_done
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.presentation.components.ExpenseSplitParticipantRow
import com.eyther.lumbridge.features.expenses.presentation.mapper.splitMethodLabel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseSplitViewModelContract
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import kotlinx.collections.immutable.ImmutableSet
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseSplitBottomSheet(
    viewModel: ExpenseSplitViewModelContract,
    onConfirmed: (ExpenseSplitSelectionDomain) -> Unit,
    onClose: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    LaunchedEffect(state.confirmed) {
        state.confirmed?.let { selection ->
            viewModel.onNavigationHandled()
            onConfirmed(selection)
        }
    }
    InnerExpenseSplitBottomSheet(
        total = state.total,
        members = state.members,
        method = state.method,
        selected = state.selected,
        custom = state.custom,
        amounts = state.amounts,
        canDecrease = state.canDecrease,
        canIncrease = state.canIncrease,
        error = state.error,
        canConfirm = state.selection != null,
        onMethod = viewModel::onMethodChanged,
        onParticipant = viewModel::onParticipantToggled,
        onCustom = viewModel::onCustomChanged,
        onStep = viewModel::onCustomStepped,
        onConfirm = viewModel::onConfirm,
        onClose = onClose,
    )
}

@Composable
private fun InnerExpenseSplitBottomSheet(
    total: String,
    members: List<MemberDomain>,
    method: ExpenseSplitMethodDomain,
    selected: Set<String>,
    custom: Map<String, String>,
    amounts: Map<String, String>,
    canDecrease: ImmutableSet<String>,
    canIncrease: ImmutableSet<String>,
    error: TextResource?,
    canConfirm: Boolean,
    onMethod: (ExpenseSplitMethodDomain) -> Unit,
    onParticipant: (String) -> Unit,
    onCustom: (String, String) -> Unit,
    onStep: (String, Boolean) -> Unit,
    onConfirm: () -> Unit,
    onClose: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.form_split),
        action = {
            LoomButton(
                text = stringResource(Res.string.split_done),
                onClick = onConfirm,
                enabled = canConfirm,
                modifier = Modifier.fillMaxWidth().testTag("confirm_expense_split"),
            )
        },
        dismissAction = {
            OutlinedButton(onClick = onClose) { Text(stringResource(Res.string.ui_cancel)) }
        },
    ) {
        Text(total, style = MaterialTheme.typography.headlineMedium)
        Box {
            var expanded by remember { mutableStateOf(false) }
            SelectorButton(
                text = splitMethodLabel(method),
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth().testTag("split_method"),
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                ExpenseSplitMethodDomain.entries.forEach { choice ->
                    DropdownMenuItem(
                        text = { Text(splitMethodLabel(choice)) },
                        onClick = {
                            expanded = false
                            onMethod(choice)
                        },
                    )
                }
            }
        }
        members.forEach { member ->
            ExpenseSplitParticipantRow(
                member = member,
                method = method,
                selected = member.id in selected,
                value = custom[member.id].orEmpty(),
                amount = amounts[member.id],
                canDecrease = member.id in canDecrease,
                canIncrease = member.id in canIncrease,
                onToggle = { onParticipant(member.id) },
                onValue = { onCustom(member.id, it) },
                onStep = { onStep(member.id, it) },
            )
            if (member != members.last())
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        error?.let { Text(it.getString(), color = MaterialTheme.colorScheme.error) }
    }
}
