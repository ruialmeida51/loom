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
import com.eyther.lumbridge.features.expenses.presentation.localization.expenseCategoryLabel
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.model.MonthlyStatementDomain
import com.eyther.lumbridge.features.insights.generated.resources.*
import com.eyther.lumbridge.features.insights.presentation.localization.localizedMessage
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

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
        message = localizedMessage(state.message),
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
    val summaryHeader =
        stringResource(Res.string.message_month_currency_household_total_minor_units_you_paid_minor)
    val expenseHeader =
        stringResource(
            Res.string.message_date_description_category_amount_minor_units_your_share_minor,
        )
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            PageHeading(
                stringResource(Res.string.ui_the_bigger_picture),
                stringResource(Res.string.ui_a_little_perspective),
                stringResource(Res.string.ui_see_where_it_went_and_where_things_stand),
            )
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = onPrevious) {
                    Text(stringResource(Res.string.ui_previous))
                }
                Text(month, style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onNext) { Text(stringResource(Res.string.ui_next)) }
            }
        }
        item { ErrorNotice(localizedMessage(error), onDismiss) }
        message?.let { message ->
            item { Text(message, color = MaterialTheme.colorScheme.secondary) }
        }
        statement?.let { statement ->
            item {
                LoomCard {
                    Text(
                        stringResource(Res.string.ui_household_spending),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        MoneyDomain(statement.totalMinor, context.currency).formatted(),
                        style = MaterialTheme.typography.displaySmall,
                    )
                    Text(
                        pluralStringResource(
                            Res.plurals.monthly_expense_count,
                            statement.expenses.size,
                            statement.expenses.size,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                LoomCard {
                    SectionTitle(stringResource(Res.string.ui_your_month))
                    StatementRow(
                        stringResource(Res.string.ui_you_paid),
                        statement.paidMinor,
                        context.currency,
                    )
                    StatementRow(
                        stringResource(Res.string.ui_your_share),
                        statement.shareMinor,
                        context.currency,
                    )
                    HorizontalDivider()
                    StatementRow(
                        stringResource(Res.string.ui_opening_balance),
                        statement.openingBalanceMinor,
                        context.currency,
                    )
                    StatementRow(
                        stringResource(Res.string.ui_closing_balance),
                        statement.closingBalanceMinor,
                        context.currency,
                    )
                    Text(
                        stringResource(
                            Res.string.ui_a_positive_balance_means_you_re_owed_money_repayments,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item { SectionTitle(stringResource(Res.string.ui_where_it_went)) }
            statement.categories.entries
                .sortedByDescending { it.value }
                .forEach { (category, amount) ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatementRow(expenseCategoryLabel(category), amount, context.currency)
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
                    stringResource(Res.string.ui_export_statement_as_csv),
                    {
                        onExport(
                            "loom-${month}.csv",
                            statement.csv(
                                context.currency,
                                context.userId,
                                summaryHeader,
                                expenseHeader,
                            ),
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
                    Text(
                        if (busy) stringResource(Res.string.ui_saving)
                        else stringResource(Res.string.ui_save_statement_snapshot),
                    )
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
