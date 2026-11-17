package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.detail_recurring
import com.eyther.lumbridge.features.expenses.generated.resources.detail_your_share
import com.eyther.lumbridge.features.expenses.presentation.mapper.expenseCategoryLabel
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseDetailsUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpenseDetailsSummaryCard(details: ExpenseDetailsUi) {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
            ) {
                Text(
                    text = expenseCategoryLabel(details.category),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                )
                if (details.recurring)
                    Text(
                        text = stringResource(Res.string.detail_recurring),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
            }
            Text(
                text = details.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = details.amount,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            details.ownShare?.let { share ->
                Row(horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium)) {
                    Text(
                        text = stringResource(Res.string.detail_your_share),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        text = share,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}
