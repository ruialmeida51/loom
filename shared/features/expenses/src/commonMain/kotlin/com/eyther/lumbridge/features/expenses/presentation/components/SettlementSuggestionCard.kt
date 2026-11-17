package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_person_owes
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.ui_record_repayment
import com.eyther.lumbridge.features.expenses.presentation.model.SuggestedRepaymentUi
import org.jetbrains.compose.resources.stringResource

/** One proposed transfer, with a personal recording action and an unambiguous amount. */
@Composable
internal fun SettlementSuggestionCard(repayment: SuggestedRepaymentUi, onRecord: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium)) {
                Text(
                    stringResource(
                        Res.string.balance_person_owes,
                        repayment.fromName ?: stringResource(Res.string.former_member),
                        repayment.toName ?: stringResource(Res.string.former_member),
                    ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    repayment.amount,
                    style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                )
            }
            if (repayment.isPersonal)
                FilledTonalButton(onClick = onRecord) {
                    Text(stringResource(Res.string.ui_record_repayment))
                }
        }
    }
}
