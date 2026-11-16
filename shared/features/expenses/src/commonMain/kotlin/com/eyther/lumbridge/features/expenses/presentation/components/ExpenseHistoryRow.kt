package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.components.ListRow
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.history_paid_by
import com.eyther.lumbridge.features.expenses.generated.resources.history_repayment
import com.eyther.lumbridge.features.expenses.generated.resources.history_transfer
import com.eyther.lumbridge.features.expenses.generated.resources.history_your_share
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseHistoryItemUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpenseHistoryRow(
    item: ExpenseHistoryItemUi,
    onOpen: (ExpenseDomain) -> Unit,
) {
    when (item) {
        is ExpenseHistoryItemUi.Purchase ->
            ListRow {
                Row(
                    modifier =
                        Modifier.fillMaxWidth()
                            .clickable { onOpen(item.expense) }
                            .padding(vertical = LoomSpacing.small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.expense.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            stringResource(
                                Res.string.history_paid_by,
                                item.payerName ?: stringResource(Res.string.former_member),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
                    ) {
                        Text(
                            item.amount,
                            style =
                                MaterialTheme.typography.titleMedium.copy(
                                    fontFeatureSettings = "tnum",
                                ),
                        )
                        Text(
                            stringResource(
                                Res.string.history_your_share,
                                item.yourShare,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        is ExpenseHistoryItemUi.Repayment ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = LoomSpacing.medium)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(Res.string.history_repayment),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        stringResource(
                            Res.string.history_transfer,
                            item.fromName ?: stringResource(Res.string.former_member),
                            item.toName ?: stringResource(Res.string.former_member),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(item.amount, style = MaterialTheme.typography.titleSmall)
            }
    }
}
