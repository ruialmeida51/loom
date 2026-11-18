package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.payment_label
import com.eyther.lumbridge.features.expenses.presentation.model.PaymentDetailsUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PaymentDetailsSummaryCard(details: PaymentDetailsUi) {
    val emphasis =
        when {
            details.isUserSender -> MaterialTheme.colorScheme.error
            details.isUserRecipient -> LoomColors.positive
            else -> MaterialTheme.colorScheme.primary
        }
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        ) {
            Text(
                text = stringResource(Res.string.payment_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text =
                    paymentTitle(
                        fromName = details.fromName,
                        toName = details.toName,
                        isUserSender = details.isUserSender,
                        isUserRecipient = details.isUserRecipient,
                    ),
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = details.amount,
                style = MaterialTheme.typography.displaySmall,
                color = emphasis,
            )
        }
    }
}
