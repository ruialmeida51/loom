package com.eyther.lumbridge.features.insights.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.model.MonthlyStatementDomain
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import kotlinx.coroutines.flow.*

@Composable
fun InsightsScreen(
    vm: InsightsScreenViewModelContract,
    context: HouseholdContextDomain,
    onExport: (String, String) -> Unit,
) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerInsightsScreen(
        month = state.month,
        statement = state.statement,
        busy = state.busy,
        message = state.message,
        error = state.error,
        context = context,
        onExport = onExport,
        onPrevious = vm::previous,
        onNext = vm::next,
        onDismiss = vm::dismiss,
        onSave = vm::save,
    )
}

@Composable
private fun InnerInsightsScreen(
    month: String,
    statement: MonthlyStatementDomain?,
    busy: Boolean,
    message: String?,
    error: String?,
    context: HouseholdContextDomain,
    onExport: (String, String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageHeading(
                "THE BIGGER PICTURE",
                "A little perspective.",
                "See where it went, and where things stand.",
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onPrevious) { Text("Previous") }
                Text(month, style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onNext) { Text("Next") }
            }
        }
        item { ErrorNotice(error, onDismiss) }
        message?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.secondary) }
        }
        statement?.let { statement ->
            item {
                LoomCard {
                    Text(
                        "HOUSEHOLD SPENDING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        MoneyDomain(statement.totalMinor, context.currency).formatted(),
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Text(
                        "${statement.expenses.size} shared expenses this month",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                LoomCard {
                    SectionTitle("Your month")
                    StatementRow("You paid", statement.paidMinor, context.currency)
                    StatementRow("Your share", statement.shareMinor, context.currency)
                    HorizontalDivider()
                    StatementRow("Opening balance", statement.openingBalanceMinor, context.currency)
                    StatementRow("Closing balance", statement.closingBalanceMinor, context.currency)
                    Text(
                        "A positive balance means you’re owed money. Repayments affect balances, not spending.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item { SectionTitle("Where it went") }
            statement.categories.entries
                .sortedByDescending { it.value }
                .forEach { (category, amount) ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatementRow(category, amount, context.currency)
                            Box(
                                Modifier.fillMaxWidth()
                                    .height(7.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        RoundedCornerShape(4.dp),
                                    ),
                            ) {
                                Box(
                                    Modifier.fillMaxWidth(
                                            if (statement.totalMinor == 0L) 0f
                                            else amount.toFloat() / statement.totalMinor,
                                        )
                                        .height(7.dp)
                                        .background(
                                            MaterialTheme.colorScheme.secondary,
                                            RoundedCornerShape(4.dp),
                                        ),
                                )
                            }
                        }
                    }
                }
            item {
                LoomButton(
                    "Export statement as CSV",
                    {
                        onExport(
                            "loom-${month}.csv",
                            statement.csv(context.currency, context.userId),
                        )
                    },
                    Modifier.fillMaxWidth(),
                )
            }
            item {
                OutlinedButton(
                    onClick = onSave,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (busy) "Saving…" else "Save statement snapshot")
                }
            }
        }
    }
}

@Composable
private fun StatementRow(label: String, amount: Long, currency: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Text(
            MoneyDomain(amount, currency).formatted(),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
