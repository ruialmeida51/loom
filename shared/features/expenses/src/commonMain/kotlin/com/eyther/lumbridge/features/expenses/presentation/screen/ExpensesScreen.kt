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
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

@Composable
fun ExpensesScreen(
    vm: ExpensesScreenViewModelContract,
    context: HouseholdContextDomain,
    onInsights: () -> Unit,
) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerExpensesScreen(
        expenses = state.expenses,
        settlements = state.settlements,
        members = state.members,
        balances = state.balances,
        title = state.title,
        amount = state.amount,
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
        onTitle = vm::title,
        onAmount = vm::amount,
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
    onTitle: (String) -> Unit,
    onAmount: (String) -> Unit,
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
    fun name(id: String) = members.firstOrNull { it.id == id }?.name ?: "Former member"
    val balance = balances[context.userId] ?: 0L
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageHeading(
                "FAIR & SQUARE",
                "Money, untangled.",
                "Shared costs. Clear balances. One less awkward conversation.",
            )
        }
        item { ErrorNotice(error, onDismiss) }
        item {
            LoomCard {
                Text(
                    if (balance >= 0) "YOU’RE OWED" else "YOU OWE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    MoneyDomain(if (balance < 0) -balance else balance, context.currency)
                        .formatted(),
                    style = MaterialTheme.typography.displaySmall,
                )
                Text(
                    if (balance == 0L) "All square. That’s a nice feeling."
                    else "Across your household",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LoomButton("Add expense", { onShowAdd(true) }, Modifier.weight(1f))
                    OutlinedButton(
                        onClick = { onShowSettle(true) },
                        modifier = Modifier.height(50.dp),
                    ) {
                        Text("Settle up")
                    }
                }
            }
        }
        item { SectionTitle("The household") }
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
                            "Owed ${MoneyDomain(memberBalance,context.currency).formatted()}"
                        memberBalance < 0 ->
                            "Owes ${MoneyDomain(-memberBalance,context.currency).formatted()}"
                        else -> "All square"
                    },
                    color =
                        if (memberBalance >= 0) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item { SectionTitle("Recent expenses", "Insights", onInsights) }
        if (expenses.isEmpty())
            item {
                EmptyNotice(
                    "Start with the next shared cost.",
                    "Add what you paid, then choose who to split it with.",
                )
            }
        items(expenses.sortedByDescending { it.occurredOn }, key = { it.id }) { expense ->
            LoomCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(expense.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${name(expense.paidBy)} paid · ${expense.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (expense.recurring)
                            Text(
                                "Monthly bill",
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
                            "Your share ${MoneyDomain(expense.shares[context.userId]?:0,context.currency).formatted()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        if (settlements.isNotEmpty()) item { SectionTitle("Repayments") }
        items(settlements, key = { it.id }) { settlement ->
            Text(
                "${name(settlement.from)} paid ${name(settlement.to)} ${MoneyDomain(settlement.amountMinor,context.currency).formatted()}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
    if (showAdd)
        AlertDialog(
            onDismissRequest = { if (!busy) onShowAdd(false) },
            title = { Text("Add a shared expense") },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        title,
                        onTitle,
                        label = { Text("What was it for?") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        amount,
                        onAmount,
                        label = { Text("Amount · ${context.currency}") },
                        singleLine = true,
                    )
                    Text("Who paid?", style = MaterialTheme.typography.labelLarge)
                    members.forEach { member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(paidBy == member.id, { onPayer(member.id) })
                            Text(member.name)
                        }
                    }
                    Text("Split with", style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Equal", "Amounts", "Percent").forEach { mode ->
                            FilterChip(
                                splitMode == mode,
                                { onSplitMode(mode) },
                                label = { Text(mode) },
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
                    Text("Category", style = MaterialTheme.typography.labelLarge)
                    listOf("Groceries", "Bills", "Food & drink", "Home", "Other").forEach { category
                        ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selectedCategory == category, { onCategory(category) })
                            Text(category)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(recurring, onRecurring)
                        Text("Repeat this bill monthly")
                    }
                    ErrorNotice(error, onDismiss)
                }
            },
            confirmButton = { LoomButton("Save expense", onAdd, loading = busy) },
            dismissButton = {
                TextButton(onClick = { onShowAdd(false) }, enabled = !busy) { Text("Cancel") }
            },
        )
    if (showSettle)
        AlertDialog(
            onDismissRequest = { if (!busy) onShowSettle(false) },
            title = { Text("Record a repayment") },
            text = {
                Column(
                    Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Money sent by", style = MaterialTheme.typography.labelLarge)
                    members.forEach { member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(from == member.id, { onFrom(member.id) })
                            Text(member.name)
                        }
                    }
                    Text("Received by", style = MaterialTheme.typography.labelLarge)
                    members.forEach { member ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(to == member.id, { onTo(member.id) })
                            Text(member.name)
                        }
                    }
                    OutlinedTextField(
                        settleAmount,
                        onSettleAmount,
                        label = { Text("Amount · ${context.currency}") },
                        singleLine = true,
                    )
                    Text(
                        "This records a payment you’ve already made or received.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    ErrorNotice(error, onDismiss)
                }
            },
            confirmButton = { LoomButton("Record repayment", onSettle, loading = busy) },
            dismissButton = {
                TextButton(onClick = { onShowSettle(false) }, enabled = !busy) { Text("Cancel") }
            },
        )
}
