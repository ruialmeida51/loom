package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.LoadingForm
import com.eyther.lumbridge.core.presentation.components.LoadingRow
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.form_date
import com.eyther.lumbridge.features.expenses.generated.resources.form_less
import com.eyther.lumbridge.features.expenses.generated.resources.form_monthly
import com.eyther.lumbridge.features.expenses.generated.resources.form_more
import com.eyther.lumbridge.features.expenses.generated.resources.form_recurring_unchanged
import com.eyther.lumbridge.features.expenses.generated.resources.form_save
import com.eyther.lumbridge.features.expenses.generated.resources.form_split
import com.eyther.lumbridge.features.expenses.generated.resources.split_participant_count
import com.eyther.lumbridge.features.expenses.generated.resources.split_summary
import com.eyther.lumbridge.features.expenses.generated.resources.ui_who_paid
import com.eyther.lumbridge.features.expenses.presentation.components.ExpenseCaptureFields
import com.eyther.lumbridge.features.expenses.presentation.mapper.accountLabel
import com.eyther.lumbridge.features.expenses.presentation.mapper.expenseCategoryLabel
import com.eyther.lumbridge.features.expenses.presentation.mapper.splitMethodLabel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.presentation.components.MemberPickerRow
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseFormScreen(
    viewModel: ExpenseFormScreenViewModelContract,
    onSaved: () -> Unit,
    onEditSplit: (Long) -> Unit,
    onChooseDate: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    LaunchedEffect(state.redirectTo) {
        when (val redirect = state.redirectTo) {
            ExpenseFormScreenViewModelContract.RedirectTo.Saved -> {
                viewModel.onNavigationHandled()
                onSaved()
            }
            is ExpenseFormScreenViewModelContract.RedirectTo.EditSplit -> {
                viewModel.onNavigationHandled()
                onEditSplit(redirect.amountMinor)
            }
            null -> Unit
        }
    }
    InnerExpenseFormScreen(
        account = state.account,
        members = state.members,
        title = state.title,
        amount = state.amount,
        paidBy = state.paidBy,
        date = state.occurredOn,
        category = state.category,
        recurring = state.recurring,
        splitMethod = state.split.method,
        participantCount = state.split.participants.size,
        showDetails = state.showDetails,
        editing = state.editing,
        readyForInput = !state.editing || state.expense != null,
        loading = state.loading,
        busy = state.busy,
        canSave = state.canSave,
        error = state.error,
        onTitle = viewModel::onTitleChanged,
        onAmount = viewModel::onAmountChanged,
        onPayer = viewModel::onPayerChanged,
        onCategory = viewModel::onCategoryChanged,
        onRecurring = viewModel::onRecurringChanged,
        onDetails = viewModel::onDetailsVisibilityChanged,
        onSplit = viewModel::onEditSplit,
        onDate = onChooseDate,
        onSave = viewModel::onSave,
        onDismiss = viewModel::onDismissError,
        onRetry = viewModel::onRetry,
    )
}

@Composable
private fun InnerExpenseFormScreen(
    account: ExpenseAccountDomain?,
    members: List<MemberDomain>,
    title: String,
    amount: String,
    paidBy: String,
    date: String,
    category: String,
    recurring: Boolean,
    splitMethod: ExpenseSplitMethodDomain,
    participantCount: Int,
    showDetails: Boolean,
    editing: Boolean,
    readyForInput: Boolean,
    loading: Boolean,
    busy: Boolean,
    canSave: Boolean,
    error: TextResource?,
    onTitle: (String) -> Unit,
    onAmount: (String) -> Unit,
    onPayer: (String) -> Unit,
    onCategory: (String) -> Unit,
    onRecurring: (Boolean) -> Unit,
    onDetails: (Boolean) -> Unit,
    onSplit: () -> Unit,
    onDate: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    Column(Modifier.fillMaxSize().imePadding()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        ) {
            if (readyForInput) {
                ExpenseCaptureFields(
                    amount = amount,
                    title = title,
                    currency = account?.currency,
                    enabled = !busy,
                    onAmount = onAmount,
                    onTitle = onTitle,
                )
            } else if (loading) LoadingForm()
            error?.let { message ->
                ErrorNotice(message = message, onDismiss = if (account == null) null else onDismiss)
                if (account == null)
                    OutlinedButton(onClick = onRetry) {
                        Text(stringResource(Res.string.balance_retry))
                    }
            }
            if (loading && account == null && readyForInput) LoadingRow()
            if (account != null) {
                Text(
                    accountLabel(account),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                GroupedSurface {
                    MemberPickerRow(
                        label = stringResource(Res.string.ui_who_paid),
                        members = members,
                        selectedId = paidBy,
                        onSelected = onPayer,
                        enabled = !busy,
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    NavigationRow(
                        title = stringResource(Res.string.form_split),
                        value =
                            stringResource(
                                Res.string.split_summary,
                                splitMethodLabel(splitMethod),
                                pluralStringResource(
                                    Res.plurals.split_participant_count,
                                    participantCount,
                                    participantCount,
                                ),
                            ),
                        onClick = onSplit,
                        enabled = !busy,
                    )
                }
                OutlinedButton(onClick = { onDetails(!showDetails) }, enabled = !busy) {
                    Text(
                        stringResource(
                            if (showDetails) Res.string.form_less else Res.string.form_more,
                        ),
                    )
                }
                if (showDetails) {
                    NavigationRow(
                        title = stringResource(Res.string.form_date),
                        description = date,
                        onClick = onDate,
                        enabled = !busy,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        listOf("Groceries", "Bills", "Food & drink", "Home", "Other").forEach {
                            choice ->
                            FilterChip(
                                selected = category == choice,
                                onClick = { onCategory(choice) },
                                enabled = !busy,
                                label = { Text(expenseCategoryLabel(choice)) },
                            )
                        }
                    }
                    if (!editing)
                        Row {
                            Text(
                                stringResource(Res.string.form_monthly),
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                modifier = Modifier.testTag("expense_recurring"),
                                checked = recurring,
                                onCheckedChange = onRecurring,
                                enabled = !busy,
                            )
                        }
                    else if (recurring)
                        Text(
                            stringResource(Res.string.form_recurring_unchanged),
                            style = MaterialTheme.typography.bodySmall,
                        )
                }
            }
        }
        LoomButton(
            text = stringResource(Res.string.form_save),
            onClick = onSave,
            enabled = canSave,
            loading = busy,
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = LoomSpacing.content, vertical = LoomSpacing.medium),
        )
    }
}
