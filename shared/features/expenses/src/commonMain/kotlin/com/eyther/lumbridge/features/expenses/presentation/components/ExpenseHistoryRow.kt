package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.history_paid_by
import com.eyther.lumbridge.features.expenses.generated.resources.history_repayment
import com.eyther.lumbridge.features.expenses.generated.resources.history_transfer
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseHistoryItemUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpenseHistoryRow(
    item: ExpenseHistoryItemUi,
    onOpen: (ExpenseDomain) -> Unit,
) {
    when (item) {
        is ExpenseHistoryItemUi.Purchase ->
            Surface(
                onClick = { onOpen(item.expense) },
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(LoomSpacing.content),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            LoomIcon(
                                glyph = Glyph.MONEY,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                decorative = true,
                            )
                        }
                    }
                    Column(
                        Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(LoomSpacing.small),
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
                            Text(
                                item.expense.title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                item.amount,
                                style =
                                    MaterialTheme.typography.titleLarge.copy(
                                        fontFeatureSettings = "tnum",
                                    ),
                            )
                        }
                        Text(
                            stringResource(
                                Res.string.history_paid_by,
                                item.payerName ?: stringResource(Res.string.former_member),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        is ExpenseHistoryItemUi.Repayment ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = LoomSpacing.content, vertical = LoomSpacing.medium),
                horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LoomIcon(
                    glyph = Glyph.CHECK,
                    tint = MaterialTheme.colorScheme.primary,
                    decorative = true,
                )
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
