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
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_person_owes
import com.eyther.lumbridge.features.expenses.generated.resources.balance_person_owes_you
import com.eyther.lumbridge.features.expenses.generated.resources.balance_you_owe_person
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.payment_i_paid_person
import com.eyther.lumbridge.features.expenses.generated.resources.payment_person_paid_me
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
                    when {
                        repayment.isCurrentUserSender ->
                            stringResource(
                                Res.string.balance_you_owe_person,
                                repayment.toName ?: stringResource(Res.string.former_member),
                            )
                        repayment.isCurrentUserRecipient ->
                            stringResource(
                                Res.string.balance_person_owes_you,
                                repayment.fromName ?: stringResource(Res.string.former_member),
                            )
                        else ->
                            stringResource(
                                Res.string.balance_person_owes,
                                repayment.fromName ?: stringResource(Res.string.former_member),
                                repayment.toName ?: stringResource(Res.string.former_member),
                            )
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    repayment.amount,
                    style = MaterialTheme.typography.titleMedium,
                    color =
                        when {
                            repayment.isCurrentUserSender -> MaterialTheme.colorScheme.error
                            repayment.isCurrentUserRecipient -> LoomColors.positive
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                )
            }
            if (repayment.isPersonal)
                FilledTonalButton(
                    onClick = onRecord,
                    modifier = Modifier.testTag("record_payment"),
                ) {
                    Text(
                        if (repayment.isCurrentUserSender)
                            stringResource(
                                Res.string.payment_i_paid_person,
                                repayment.toName ?: stringResource(Res.string.former_member),
                            )
                        else
                            stringResource(
                                Res.string.payment_person_paid_me,
                                repayment.fromName ?: stringResource(Res.string.former_member),
                            ),
                    )
                }
        }
    }
}
