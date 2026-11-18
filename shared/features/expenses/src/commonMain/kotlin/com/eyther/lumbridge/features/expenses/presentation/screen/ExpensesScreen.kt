package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ActionFab
import com.eyther.lumbridge.core.presentation.components.DestructiveConfirmation
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.FloatingActionContentPadding
import com.eyther.lumbridge.core.presentation.components.LoadingRows
import com.eyther.lumbridge.core.presentation.components.SelectorButton
import com.eyther.lumbridge.core.presentation.components.container.PullToRefreshContentContainer
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_accounts
import com.eyther.lumbridge.features.expenses.generated.resources.account_archived
import com.eyther.lumbridge.features.expenses.generated.resources.account_manage
import com.eyther.lumbridge.features.expenses.generated.resources.balance_everyone
import com.eyther.lumbridge.features.expenses.generated.resources.balance_retry
import com.eyther.lumbridge.features.expenses.generated.resources.balance_who_pays
import com.eyther.lumbridge.features.expenses.generated.resources.history_balances
import com.eyther.lumbridge.features.expenses.generated.resources.history_empty
import com.eyther.lumbridge.features.expenses.generated.resources.history_expenses
import com.eyther.lumbridge.features.expenses.generated.resources.ui_add_expense
import com.eyther.lumbridge.features.expenses.generated.resources.ui_recurring_expenses
import com.eyther.lumbridge.features.expenses.presentation.components.ExpenseHistoryRow
import com.eyther.lumbridge.features.expenses.presentation.components.MemberBalanceRow
import com.eyther.lumbridge.features.expenses.presentation.components.PersonalBalanceCard
import com.eyther.lumbridge.features.expenses.presentation.components.SettlementSuggestionCard
import com.eyther.lumbridge.features.expenses.presentation.mapper.accountLabel
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseHistoryItemUi
import com.eyther.lumbridge.features.expenses.presentation.model.ExpensesViewUi
import com.eyther.lumbridge.features.expenses.presentation.model.MemberBalanceUi
import com.eyther.lumbridge.features.expenses.presentation.model.SuggestedRepaymentUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpensesScreen(
    viewModel: ExpensesScreenViewModelContract,
    onAddExpense: () -> Unit,
    onOpenExpense: (ExpenseDomain) -> Unit,
    onEditExpense: (ExpenseDomain) -> Unit,
    onRecurringExpenses: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    PullToRefreshContentContainer(
        isRefreshing = state.refreshing,
        onRefresh = viewModel::onRefresh,
        floatingActionButton = {
            if (state.canCreateExpense)
                ActionFab(
                    text = stringResource(Res.string.ui_add_expense),
                    onClick = onAddExpense,
                    modifier = Modifier.testTag("add_expense"),
                )
        },
    ) {
        InnerExpensesScreen(
            account = state.account,
            history = state.history,
            selectedView = state.selectedView,
            memberBalances = state.memberBalances,
            suggestions = state.suggestedRepayments,
            ownBalance = state.ownBalance,
            loading = state.loading,
            error = state.displayError,
            onAccounts = { viewModel.onAccountsVisibilityChanged(true) },
            onOpenExpense = onOpenExpense,
            onEditExpense = onEditExpense,
            onDeleteExpense = viewModel::onDeleteExpenseRequested,
            onEditSettlement = viewModel::onEditSettlement,
            onDeleteSettlement = viewModel::onDeleteSettlementRequested,
            busy = state.busy,
            onManage = { viewModel.onManageAccountsVisibilityChanged(true) },
            onView = viewModel::onViewChanged,
            onRepayment = viewModel::onSuggestedRepayment,
            onRecurringExpenses = onRecurringExpenses,
            onDismiss = viewModel::onDismissError,
            onRetry = viewModel::onRefresh,
        )
    }
    state.entryDeleteConfirmation?.let { confirmation ->
        DestructiveConfirmation(
            confirmation = confirmation,
            busy = state.busy,
            error = state.entryDeleteError,
            confirmationTag =
                if (state.deletingExpense != null) "confirm_delete_expense_entry"
                else "confirm_delete_payment",
            onConfirm = viewModel::onEntryDeleteConfirmed,
            onDismiss = viewModel::onEntryDeleteCancelled,
        )
    }
}

@Composable
private fun InnerExpensesScreen(
    account: ExpenseAccountDomain?,
    history: ImmutableList<ExpenseHistoryItemUi>,
    selectedView: ExpensesViewUi,
    memberBalances: ImmutableList<MemberBalanceUi>,
    suggestions: ImmutableList<SuggestedRepaymentUi>,
    ownBalance: BalanceSummaryUi,
    loading: Boolean,
    error: TextResource?,
    onAccounts: () -> Unit,
    onOpenExpense: (ExpenseDomain) -> Unit,
    onEditExpense: (ExpenseDomain) -> Unit,
    onDeleteExpense: (ExpenseDomain) -> Unit,
    onEditSettlement: (SettlementDomain) -> Unit,
    onDeleteSettlement: (SettlementDomain) -> Unit,
    busy: Boolean,
    onManage: () -> Unit,
    onView: (ExpensesViewUi) -> Unit,
    onRepayment: (SuggestedSettlementDomain) -> Unit,
    onRecurringExpenses: () -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier =
                Modifier.fillMaxSize().padding(horizontal = 16.dp).testTag("expenses_content"),
            contentPadding =
                PaddingValues(top = LoomSpacing.small, bottom = FloatingActionContentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SelectorButton(
                        text =
                            if (account == null) stringResource(Res.string.account_accounts)
                            else accountLabel(account),
                        onClick = onAccounts,
                        modifier = Modifier.weight(1f).testTag("expense_accounts"),
                    )
                    ActionMenu(
                        modifier = Modifier.testTag("balances_actions"),
                        items =
                            buildList {
                                add(
                                    ActionMenuItem(
                                        text =
                                            TextResource.Resource(Res.string.ui_recurring_expenses),
                                        onClick = onRecurringExpenses,
                                    ),
                                )
                                add(
                                    ActionMenuItem(
                                        text = TextResource.Resource(Res.string.account_manage),
                                        onClick = onManage,
                                    ),
                                )
                            },
                    )
                }
                if (account?.archived == true)
                    Text(
                        stringResource(Res.string.account_archived),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
            }
            item {
                Column(modifier = Modifier.padding(bottom = LoomSpacing.medium)) {
                    TabRow(selectedTabIndex = selectedView.ordinal) {
                        Tab(
                            modifier = Modifier.testTag("balances_view_expenses"),
                            selected = selectedView == ExpensesViewUi.EXPENSES,
                            onClick = { onView(ExpensesViewUi.EXPENSES) },
                            text = { Text(stringResource(Res.string.history_expenses)) },
                        )
                        Tab(
                            modifier = Modifier.testTag("balances_view_balances"),
                            selected = selectedView == ExpensesViewUi.BALANCES,
                            onClick = { onView(ExpensesViewUi.BALANCES) },
                            text = { Text(stringResource(Res.string.history_balances)) },
                        )
                    }
                }
            }
            error?.let { message ->
                item {
                    ErrorNotice(message = message, onDismiss = onDismiss)
                    OutlinedButton(onClick = onRetry) {
                        Text(stringResource(Res.string.balance_retry))
                    }
                }
            }
            if (loading) item { LoadingRows() }
            else if (account != null && selectedView == ExpensesViewUi.EXPENSES) {
                if (history.isEmpty())
                    item {
                        Text(
                            stringResource(Res.string.history_empty),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                items(history, key = { it.key }) { item ->
                    ExpenseHistoryRow(
                        item = item,
                        onOpen = onOpenExpense,
                        enabled = !busy,
                        onEditExpense = onEditExpense,
                        onDeleteExpense = onDeleteExpense,
                        onEditSettlement = onEditSettlement,
                        onDeleteSettlement = onDeleteSettlement,
                    )
                }
            } else if (account != null) {
                item(key = "current_position") {
                    PersonalBalanceCard(
                        balance = ownBalance,
                        everyoneSettled = suggestions.isEmpty(),
                    )
                }
                if (suggestions.isNotEmpty())
                    item {
                        Text(
                            stringResource(Res.string.balance_who_pays),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                items(suggestions, key = { "${it.settlement.from}-${it.settlement.to}" }) {
                    repayment ->
                    SettlementSuggestionCard(
                        repayment = repayment,
                        onRecord = { onRepayment(repayment.settlement) },
                    )
                }
                item {
                    Text(
                        stringResource(Res.string.balance_everyone),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                items(memberBalances, key = { it.member.id }) { member ->
                    MemberBalanceRow(member = member)
                }
            }
        }
    }
}
