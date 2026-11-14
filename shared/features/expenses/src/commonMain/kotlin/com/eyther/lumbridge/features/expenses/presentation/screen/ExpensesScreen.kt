package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.GroupedSurface
import com.eyther.lumbridge.core.presentation.components.ListRow
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.NavigationRow
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.ui_1_s_paid_2_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_1_s_paid_2_s_3_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_across_your_household
import com.eyther.lumbridge.features.expenses.generated.resources.ui_add_expense
import com.eyther.lumbridge.features.expenses.generated.resources.ui_add_what_you_paid_then_choose_who_to_split
import com.eyther.lumbridge.features.expenses.generated.resources.ui_all_square
import com.eyther.lumbridge.features.expenses.generated.resources.ui_all_square_that_s_a_nice_feeling
import com.eyther.lumbridge.features.expenses.generated.resources.ui_former_member
import com.eyther.lumbridge.features.expenses.generated.resources.ui_insights
import com.eyther.lumbridge.features.expenses.generated.resources.ui_money_untangled
import com.eyther.lumbridge.features.expenses.generated.resources.ui_monthly_bill
import com.eyther.lumbridge.features.expenses.generated.resources.ui_owed_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_owes_1_s
import com.eyther.lumbridge.features.expenses.generated.resources.ui_recent_expenses
import com.eyther.lumbridge.features.expenses.generated.resources.ui_recurring_expenses
import com.eyther.lumbridge.features.expenses.generated.resources.ui_repayments
import com.eyther.lumbridge.features.expenses.generated.resources.ui_settle_up
import com.eyther.lumbridge.features.expenses.generated.resources.ui_shared_costs_clear_balances_one_less_awkward_conversation
import com.eyther.lumbridge.features.expenses.generated.resources.ui_start_with_the_next_shared_cost
import com.eyther.lumbridge.features.expenses.generated.resources.ui_the_household
import com.eyther.lumbridge.features.expenses.generated.resources.ui_you_owe
import com.eyther.lumbridge.features.expenses.generated.resources.ui_you_re_owed
import com.eyther.lumbridge.features.expenses.generated.resources.ui_your_share_1_s
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import com.eyther.lumbridge.features.expenses.presentation.model.MemberBalanceUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpensesScreen(
    viewModel: ExpensesScreenViewModelContract,
    context: HouseholdContextDomain,
    onInsights: () -> Unit,
    onRecurringExpenses: () -> Unit,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerExpensesScreen(
        expenses = state.expenses,
        settlements = state.settlements,
        balance = state.ownBalance,
        memberBalances = state.memberBalances,
        memberNames = state.memberNames,
        error = state.displayError,
        canCreateExpense = state.canCreateExpense,
        canRecordSettlement = state.canRecordSettlement,
        context = context,
        onInsights = onInsights,
        onRecurringExpenses = onRecurringExpenses,
        onShowAdd = viewModel::onAddSheetVisibilityChanged,
        onShowSettle = viewModel::onSettlementSheetVisibilityChanged,
        onDismiss = viewModel::onDismissError,
    )
}

@Composable
private fun InnerExpensesScreen(
    expenses: List<ExpenseDomain>,
    settlements: List<SettlementDomain>,
    balance: BalanceSummaryUi,
    memberBalances: List<MemberBalanceUi>,
    memberNames: Map<String, String>,
    error: TextResource?,
    canCreateExpense: Boolean,
    canRecordSettlement: Boolean,
    context: HouseholdContextDomain,
    onInsights: () -> Unit,
    onRecurringExpenses: () -> Unit,
    onShowAdd: (Boolean) -> Unit,
    onShowSettle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val formerMemberLabel = stringResource(Res.string.ui_former_member)
    fun name(id: String) = memberNames[id] ?: formerMemberLabel
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageHeading(
                stringResource(Res.string.ui_money_untangled),
                stringResource(
                    Res.string.ui_shared_costs_clear_balances_one_less_awkward_conversation,
                ),
            )
        }
        error?.let { message ->
            item(key = "error") {
                ErrorNotice(message = message, onDismiss = onDismiss)
            }
        }
        item {
            LoomCard {
                Text(
                    if (balance.isOwed) stringResource(Res.string.ui_you_re_owed)
                    else stringResource(Res.string.ui_you_owe),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    balance.amount,
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    if (balance.isSettled)
                        stringResource(Res.string.ui_all_square_that_s_a_nice_feeling)
                    else stringResource(Res.string.ui_across_your_household),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LoomButton(
                        stringResource(Res.string.ui_add_expense),
                        { onShowAdd(true) },
                        Modifier.weight(1f),
                        enabled = canCreateExpense,
                    )
                    OutlinedButton(
                        onClick = { onShowSettle(true) },
                        enabled = canRecordSettlement,
                        modifier = Modifier.heightIn(min = 50.dp),
                    ) {
                        Text(stringResource(Res.string.ui_settle_up))
                    }
                }
            }
        }
        item {
            GroupedSurface {
                NavigationRow(
                    title = stringResource(Res.string.ui_recurring_expenses),
                    onClick = onRecurringExpenses,
                )
            }
        }
        item { SectionTitle(stringResource(Res.string.ui_the_household)) }
        items(memberBalances, key = { it.member.id }) { row ->
            val member = row.member
            val memberBalance = row.balance
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(member.name)
                Text(
                    when {
                        !memberBalance.isSettled && memberBalance.isOwed ->
                            stringResource(
                                Res.string.ui_owed_1_s,
                                memberBalance.amount,
                            )
                        !memberBalance.isOwed ->
                            stringResource(
                                Res.string.ui_owes_1_s,
                                memberBalance.amount,
                            )
                        else -> stringResource(Res.string.ui_all_square)
                    },
                    color =
                        if (memberBalance.isOwed) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            SectionTitle(
                stringResource(Res.string.ui_recent_expenses),
                stringResource(Res.string.ui_insights),
                onInsights,
            )
        }
        if (expenses.isEmpty())
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_start_with_the_next_shared_cost),
                    stringResource(Res.string.ui_add_what_you_paid_then_choose_who_to_split),
                )
            }
        items(expenses, key = { it.id }) { expense ->
            ListRow {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(expense.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(
                                Res.string.ui_1_s_paid_2_s,
                                name(expense.paidBy),
                                expense.category,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (expense.recurring)
                            Text(
                                stringResource(Res.string.ui_monthly_bill),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            MoneyDomain(expense.amountMinor, context.currency).formatted(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            stringResource(
                                Res.string.ui_your_share_1_s,
                                MoneyDomain(expense.shares[context.userId] ?: 0, context.currency)
                                    .formatted(),
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (settlements.isNotEmpty())
            item { SectionTitle(stringResource(Res.string.ui_repayments)) }
        items(settlements, key = { it.id }) { settlement ->
            Text(
                stringResource(
                    Res.string.ui_1_s_paid_2_s_3_s,
                    name(settlement.from),
                    name(settlement.to),
                    MoneyDomain(settlement.amountMinor, context.currency).formatted(),
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
