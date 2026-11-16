package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveButton
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete_message
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete_title
import com.eyther.lumbridge.features.expenses.generated.resources.detail_edit
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.history_paid_by
import com.eyther.lumbridge.features.expenses.generated.resources.ui_cancel
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseDetailsScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpenseDetailsScreen(
    viewModel: ExpenseDetailsScreenViewModelContract,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    LaunchedEffect(Unit) { viewModel.onRefresh() }
    LaunchedEffect(state.deleted) { if (state.deleted) onDeleted() }
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
    ) {
        InnerExpenseDetailsScreen(
            expense = state.expense,
            currency = state.account?.currency.orEmpty(),
            memberNames = state.memberNames,
            canEdit = state.canEdit,
            loading = state.loading || state.refreshing,
            busy = state.busy,
            error = state.error,
            onEdit = onEdit,
            onDelete = viewModel::onDeleteRequested,
            onRetry = viewModel::onRefresh,
            onDismiss = viewModel::onDismissError,
        )
    }
    if (state.showDeleteConfirmation)
        AlertDialog(
            onDismissRequest = viewModel::onDeleteCancelled,
            title = { Text(stringResource(Res.string.detail_delete_title)) },
            text = { Text(stringResource(Res.string.detail_delete_message)) },
            confirmButton = {
                DestructiveButton(
                    text = stringResource(Res.string.detail_delete),
                    onClick = viewModel::onDeleteConfirmed,
                    loading = state.busy,
                    modifier = Modifier.testTag("confirm_delete_expense"),
                )
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDeleteCancelled, enabled = !state.busy) {
                    Text(stringResource(Res.string.ui_cancel))
                }
            },
        )
}

@Composable
private fun InnerExpenseDetailsScreen(
    expense: ExpenseDomain?,
    currency: String,
    memberNames: Map<String, String>,
    canEdit: Boolean,
    loading: Boolean,
    busy: Boolean,
    error: TextResource?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize()
            .testTag("expense_details_content")
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (loading) LoadingRows()
        error?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
            TextButton(onClick = onRetry) { Text(stringResource(Res.string.balance_retry)) }
        }
        if (expense != null && !loading) {
            Text(expense.title, style = MaterialTheme.typography.titleLarge)
            Text(
                MoneyDomain(expense.amountMinor, currency).formatted(),
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                stringResource(
                    Res.string.history_paid_by,
                    memberNames[expense.paidBy] ?: stringResource(Res.string.former_member),
                ),
            )
            Text(expense.occurredOn, color = MaterialTheme.colorScheme.onSurfaceVariant)
            expense.shares.forEach { (id, amount) ->
                Row {
                    Text(
                        memberNames[id] ?: stringResource(Res.string.former_member),
                        modifier = Modifier.weight(1f),
                    )
                    Text(MoneyDomain(amount, currency).formatted())
                }
            }
            if (canEdit) {
                LoomButton(
                    text = stringResource(Res.string.detail_edit),
                    onClick = onEdit,
                    loading = busy,
                )
                DestructiveButton(
                    text = stringResource(Res.string.detail_delete),
                    onClick = onDelete,
                    enabled = !busy,
                )
            }
        }
    }
}
