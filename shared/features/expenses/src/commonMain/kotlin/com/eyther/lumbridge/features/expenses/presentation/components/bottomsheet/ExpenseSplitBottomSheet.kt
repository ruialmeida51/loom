package com.eyther.lumbridge.features.expenses.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.form_split
import com.eyther.lumbridge.features.expenses.generated.resources.split_decrease
import com.eyther.lumbridge.features.expenses.generated.resources.split_done
import com.eyther.lumbridge.features.expenses.generated.resources.split_increase
import com.eyther.lumbridge.features.expenses.generated.resources.split_input_amount
import com.eyther.lumbridge.features.expenses.generated.resources.split_input_percent
import com.eyther.lumbridge.features.expenses.generated.resources.split_input_shares
import com.eyther.lumbridge.features.expenses.generated.resources.split_value
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
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
            TextButton(onClick = onClose) { Text(stringResource(Res.string.ui_cancel)) }
        },
    ) {
        Text(total, style = MaterialTheme.typography.headlineMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ExpenseSplitMethodDomain.entries.forEach { choice ->
                FilterChip(
                    selected = method == choice,
                    onClick = { onMethod(choice) },
                    label = { Text(splitMethodLabel(choice)) },
                )
            }
        }
        members.forEach { member ->
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = member.id in selected,
                        onCheckedChange = { onParticipant(member.id) },
                        modifier = Modifier.semantics { contentDescription = member.name },
                    )
                    Text(
                        member.name,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleSmall,
                    )
                    amounts[member.id]?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (method != ExpenseSplitMethodDomain.EQUAL && member.id in selected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val decreaseLabel = stringResource(Res.string.split_decrease, member.name)
                        val increaseLabel = stringResource(Res.string.split_increase, member.name)
                        FilledTonalIconButton(
                            onClick = { onStep(member.id, false) },
                            enabled = member.id in canDecrease,
                            modifier =
                                Modifier.testTag("split_decrease_${member.id}").semantics {
                                    contentDescription = decreaseLabel
                                },
                        ) {
                            Text("−", style = MaterialTheme.typography.titleLarge)
                        }
                        val participantLabel = stringResource(Res.string.split_value, member.name)
                        OutlinedTextField(
                            value = custom[member.id].orEmpty(),
                            onValueChange = { onCustom(member.id, it) },
                            singleLine = true,
                            keyboardOptions =
                                KeyboardOptions(
                                    keyboardType =
                                        if (method == ExpenseSplitMethodDomain.AMOUNTS)
                                            KeyboardType.Decimal
                                        else KeyboardType.Number,
                                ),
                            label = {
                                Text(
                                    stringResource(
                                        when (method) {
                                            ExpenseSplitMethodDomain.AMOUNTS ->
                                                Res.string.split_input_amount
                                            ExpenseSplitMethodDomain.PERCENTAGES ->
                                                Res.string.split_input_percent
                                            else -> Res.string.split_input_shares
                                        },
                                    ),
                                )
                            },
                            modifier =
                                Modifier.weight(1f).testTag("split_${member.id}").semantics {
                                    contentDescription = participantLabel
                                },
                        )
                        FilledTonalIconButton(
                            onClick = { onStep(member.id, true) },
                            enabled = member.id in canIncrease,
                            modifier =
                                Modifier.testTag("split_increase_${member.id}").semantics {
                                    contentDescription = increaseLabel
                                },
                        ) {
                            Text("+", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
        }
        error?.let { Text(it.getString(), color = MaterialTheme.colorScheme.error) }
    }
}
