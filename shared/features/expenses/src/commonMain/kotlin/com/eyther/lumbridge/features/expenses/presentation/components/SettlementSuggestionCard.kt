package com.eyther.lumbridge.features.expenses.presentation.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_person_owes
import com.eyther.lumbridge.features.expenses.generated.resources.balance_person_owes_you
import com.eyther.lumbridge.features.expenses.generated.resources.balance_you_owe_person
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.payment_record
import com.eyther.lumbridge.features.expenses.presentation.model.SuggestedRepaymentUi
import org.jetbrains.compose.resources.stringResource

/** One proposed transfer, with a personal recording action and an unambiguous amount. */
@Composable
internal fun SettlementSuggestionCard(repayment: SuggestedRepaymentUi, onRecord: () -> Unit) {
    Surface(
        onClick = onRecord,
        enabled = repayment.recordEnabled,
        modifier =
            Modifier.fillMaxWidth()
                .then(
                    if (repayment.canRecord) Modifier.testTag("record_payment") else Modifier,
                ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            Modifier.padding(LoomSpacing.content),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LoomSpacing.medium),
            ) {
                MemberAvatar(
                    memberId = repayment.counterpartyId,
                    name = repayment.counterpartyName,
                    picturePath = repayment.counterpartyAvatarPath,
                )
                Column(modifier = Modifier.weight(1f)) {
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
                        style = MaterialTheme.typography.titleSmall,
                    )
                    if (repayment.canRecord)
                        Text(
                            text = stringResource(Res.string.payment_record),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                }
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
                if (repayment.canRecord) LoomIcon(glyph = Glyph.CHEVRON_RIGHT, decorative = true, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
            }
        }
    }
}
