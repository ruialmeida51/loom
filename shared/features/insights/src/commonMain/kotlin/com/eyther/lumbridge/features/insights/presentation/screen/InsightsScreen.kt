package com.eyther.lumbridge.features.insights.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.components.SectionTitle
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.presentation.mapper.expenseCategoryLabel
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.insights.domain.model.MonthlyStatementDomain
import com.eyther.lumbridge.features.insights.generated.resources.Res
import com.eyther.lumbridge.features.insights.generated.resources.monthly_expense_count
import com.eyther.lumbridge.features.insights.generated.resources.statement_csv_expense_header
import com.eyther.lumbridge.features.insights.generated.resources.statement_csv_summary_header
import com.eyther.lumbridge.features.insights.generated.resources.statement_export_title
import com.eyther.lumbridge.features.insights.generated.resources.ui_a_little_perspective
import com.eyther.lumbridge.features.insights.generated.resources.ui_a_positive_balance_means_you_re_owed_money_repayments
import com.eyther.lumbridge.features.insights.generated.resources.ui_closing_balance
import com.eyther.lumbridge.features.insights.generated.resources.ui_export_statement_as_csv
import com.eyther.lumbridge.features.insights.generated.resources.ui_household_spending
import com.eyther.lumbridge.features.insights.generated.resources.ui_next
import com.eyther.lumbridge.features.insights.generated.resources.ui_opening_balance
import com.eyther.lumbridge.features.insights.generated.resources.ui_previous
import com.eyther.lumbridge.features.insights.generated.resources.ui_save_statement_snapshot
import com.eyther.lumbridge.features.insights.generated.resources.ui_saving
import com.eyther.lumbridge.features.insights.generated.resources.ui_see_where_it_went_and_where_things_stand
import com.eyther.lumbridge.features.insights.generated.resources.ui_the_bigger_picture
import com.eyther.lumbridge.features.insights.generated.resources.ui_where_it_went
import com.eyther.lumbridge.features.insights.generated.resources.ui_you_paid
import com.eyther.lumbridge.features.insights.generated.resources.ui_your_month
import com.eyther.lumbridge.features.insights.generated.resources.ui_your_share
import com.eyther.lumbridge.features.insights.presentation.viewmodel.contracts.InsightsScreenViewModelContract
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun InsightsScreen(
    viewModel: InsightsScreenViewModelContract,
    context: HouseholdContextDomain,
) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerInsightsScreen(
        month = state.month,
        statement = state.statement,
        busy = state.busy,
        message = state.message,
        error = state.error,
        context = context,
        onExport = viewModel::onExportStatement,
        onPrevious = viewModel::onPreviousMonth,
        onNext = viewModel::onNextMonth,
        onDismiss = viewModel::onDismissMessage,
        onSave = viewModel::onSaveStatement,
    )
}

@Composable
private fun InnerInsightsScreen(
    month: String,
    statement: MonthlyStatementDomain?,
    busy: Boolean,
    message: TextResource?,
    error: TextResource?,
    context: HouseholdContextDomain,
    onExport: (String, String, String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    val exportTitle = stringResource(Res.string.statement_export_title)
    val summaryHeader =
        stringResource(Res.string.statement_csv_summary_header)
    val expenseHeader =
        stringResource(
            Res.string.statement_csv_expense_header,
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
        item { ErrorNotice(error, onDismiss) }
        message?.let { message ->
            item { Text(message.getString(), color = MaterialTheme.colorScheme.secondary) }
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
                                currency = context.currency,
                                userId = context.userId,
                                summaryHeader = summaryHeader,
                                expenseHeader = expenseHeader,
                            ),
                            exportTitle,
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
