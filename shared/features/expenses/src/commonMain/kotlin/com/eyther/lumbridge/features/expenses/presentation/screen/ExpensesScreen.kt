package com.eyther.lumbridge.features.expenses.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.generated.resources.*
import com.eyther.lumbridge.features.expenses.presentation.localization.localizedMessage
import com.eyther.lumbridge.features.expenses.presentation.localization.localizedToken
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import org.jetbrains.compose.resources.stringResource

@Composable
fun ExpensesScreen(
    vm: ExpensesScreenViewModelContract,
    context: HouseholdContextDomain,
    onInsights: () -> Unit,
    onRecurringExpenses: () -> Unit,
) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerExpensesScreen(
        expenses = state.expenses,
        settlements = state.settlements,
        members = state.members,
        balances = state.balances,
        title = state.title,
        amount = state.amount,
        occurredOn = state.occurredOn,
        paidBy = state.paidBy,
        selectedCategory = state.category,
        selected = state.selected,
        splitMode = state.splitMode,
        custom = state.custom,
        recurring = state.recurring,
        showAdd = state.showAdd,
        showSettle = state.showSettle,
        from = state.from,
        to = state.to,
        settleAmount = state.settleAmount,
        busy = state.busy,
        error = state.error,
        context = context,
        onInsights = onInsights,
        onRecurringExpenses = onRecurringExpenses,
        onTitle = vm::title,
        onAmount = vm::amount,
        onOccurredOn = vm::occurredOn,
        onPayer = vm::payer,
        onCategory = vm::category,
        onSelected = vm::selected,
        onSplitMode = vm::splitMode,
        onCustom = vm::custom,
        onRecurring = vm::recurring,
        onShowAdd = vm::showAdd,
        onShowSettle = vm::showSettle,
        onFrom = vm::from,
        onTo = vm::to,
        onSettleAmount = vm::settleAmount,
        onAdd = vm::add,
        onSettle = vm::settle,
        onDismiss = vm::dismiss,
    )
}

@Composable
private fun InnerExpensesScreen(
    expenses: List<ExpenseDomain>,
    settlements: List<SettlementDomain>,
    members: List<MemberDomain>,
    balances: Map<String, Long>,
    title: String,
    amount: String,
    occurredOn: String,
    paidBy: String,
    selectedCategory: String,
    selected: Set<String>,
    splitMode: String,
    custom: Map<String, String>,
    recurring: Boolean,
    showAdd: Boolean,
    showSettle: Boolean,
    from: String,
    to: String,
    settleAmount: String,
    busy: Boolean,
    error: String?,
    context: HouseholdContextDomain,
    onInsights: () -> Unit,
    onRecurringExpenses: () -> Unit,
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
    onShowSettle: (Boolean) -> Unit,
    onFrom: (String) -> Unit,
    onTo: (String) -> Unit,
    onSettleAmount: (String) -> Unit,
    onAdd: () -> Unit,
    onSettle: () -> Unit,
    onDismiss: () -> Unit,
) {
    val formerMemberLabel = stringResource(Res.string.ui_former_member)
    fun name(id: String) = members.firstOrNull { it.id == id }?.name ?: formerMemberLabel
    val balance = balances[context.userId] ?: 0L
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TextButton(onClick = onRecurringExpenses) {
                Text(stringResource(Res.string.ui_recurring_expenses))
            }
        }
        item {
            PageHeading(
                stringResource(Res.string.ui_fair_square),
                stringResource(Res.string.ui_money_untangled),
                stringResource(
                    Res.string.ui_shared_costs_clear_balances_one_less_awkward_conversation,
                ),
            )
        }
        item { ErrorNotice(localizedMessage(error), onDismiss) }
        item {
            LoomCard {
                Text(
                    if (balance >= 0) stringResource(Res.string.ui_you_re_owed)
                    else stringResource(Res.string.ui_you_owe),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    MoneyDomain(if (balance < 0) -balance else balance, context.currency)
                        .formatted(),
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    if (balance == 0L)
                        stringResource(Res.string.ui_all_square_that_s_a_nice_feeling)
                    else stringResource(Res.string.ui_across_your_household),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LoomButton(
                        stringResource(Res.string.ui_add_expense),
                        { onShowAdd(true) },
                        Modifier.weight(1f),
                    )
                    OutlinedButton(
                        onClick = { onShowSettle(true) },
                        modifier = Modifier.height(50.dp),
                    ) {
                        Text(stringResource(Res.string.ui_settle_up))
                    }
                }
            }
        }
        item { SectionTitle(stringResource(Res.string.ui_the_household)) }
        items(members, key = { it.id }) { member ->
            val memberBalance = balances[member.id] ?: 0L
            Row(
                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(member.name)
                Text(
                    when {
                        memberBalance > 0 ->
                            stringResource(
                                Res.string.ui_owed_1_s,
                                MoneyDomain(memberBalance, context.currency).formatted(),
                            )
                        memberBalance < 0 ->
                            stringResource(
                                Res.string.ui_owes_1_s,
                                MoneyDomain(-memberBalance, context.currency).formatted(),
                            )
                        else -> stringResource(Res.string.ui_all_square)
                    },
                    color =
                        if (memberBalance >= 0) MaterialTheme.colorScheme.secondary
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
        items(expenses.sortedByDescending { it.occurredOn }, key = { it.id }) { expense ->
            LoomCard {
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
    if (showAdd)
        AlertDialog(
            onDismissRequest = { if (!busy) onShowAdd(false) },
            title = { Text(stringResource(Res.string.ui_add_a_shared_expense)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        title,
                        onTitle,
                        label = { Text(stringResource(Res.string.ui_what_was_it_for)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        amount,
                        onAmount,
                        label = {
                            Text(stringResource(Res.string.ui_amount_1_s, context.currency))
                        },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        occurredOn,
                        onOccurredOn,
                        label = { Text(stringResource(Res.string.ui_payment_date)) },
                        singleLine = true,
                    )
                    Text(
                        stringResource(Res.string.ui_who_paid),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    members.forEach { member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(paidBy == member.id, { onPayer(member.id) })
                            Text(member.name)
                        }
                    }
                    Text(
                        stringResource(Res.string.ui_split_with),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Equal", "Amounts", "Percent").forEach { mode ->
                            FilterChip(
                                splitMode == mode,
                                { onSplitMode(mode) },
                                label = { Text(localizedToken(mode)) },
                            )
                        }
                    }
                    members.forEach { member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(member.id in selected, { onSelected(member.id) })
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
                                )
                        }
                    }
                    Text(
                        stringResource(Res.string.ui_category),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    listOf("Groceries", "Bills", "Food & drink", "Home", "Other").forEach { category
                        ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selectedCategory == category, { onCategory(category) })
                            Text(localizedToken(category))
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            recurring,
                            onRecurring,
                            modifier = Modifier.testTag("expense_recurring"),
                        )
                        Text(stringResource(Res.string.ui_repeat_this_bill_monthly))
                    }
                    if (recurring)
                        Text(
                            stringResource(Res.string.ui_repeat_explanation),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    ErrorNotice(localizedMessage(error), onDismiss)
                }
            },
            confirmButton = {
                LoomButton(stringResource(Res.string.ui_save_expense), onAdd, loading = busy)
            },
            dismissButton = {
                TextButton(onClick = { onShowAdd(false) }, enabled = !busy) {
                    Text(stringResource(Res.string.ui_cancel))
                }
            },
        )
    if (showSettle)
        AlertDialog(
            onDismissRequest = { if (!busy) onShowSettle(false) },
            title = { Text(stringResource(Res.string.ui_record_a_repayment)) },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        stringResource(Res.string.ui_money_sent_by),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    members.forEach { member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(from == member.id, { onFrom(member.id) })
                            Text(member.name)
                        }
                    }
                    Text(
                        stringResource(Res.string.ui_received_by),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    members.forEach { member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(to == member.id, { onTo(member.id) })
                            Text(member.name)
                        }
                    }
                    OutlinedTextField(
                        settleAmount,
                        onSettleAmount,
                        label = {
                            Text(stringResource(Res.string.ui_amount_1_s, context.currency))
                        },
                        singleLine = true,
                    )
                    Text(
                        stringResource(Res.string.ui_this_records_a_payment_you_ve_already_made_or),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    ErrorNotice(localizedMessage(error), onDismiss)
                }
            },
            confirmButton = {
                LoomButton(stringResource(Res.string.ui_record_repayment), onSettle, loading = busy)
            },
            dismissButton = {
                TextButton(onClick = { onShowSettle(false) }, enabled = !busy) {
                    Text(stringResource(Res.string.ui_cancel))
                }
            },
        )
}
