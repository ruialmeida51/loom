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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.IconTile
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.components.cardSegment
import com.eyther.lumbridge.core.presentation.components.cardSegmentShape
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.detail_delete
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.history_edit
import com.eyther.lumbridge.features.expenses.generated.resources.history_paid_on
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseHistoryItemUi
import org.jetbrains.compose.resources.stringResource

private val EntryIconSize = 40.dp // Round badge, shared by expenses and recorded payments.

/** Matches the overview list's item spacing, which the joined card bridges. */
internal val HistoryRowGap = LoomSpacing.small

@Composable
internal fun ExpenseHistoryRow(
    item: ExpenseHistoryItemUi,
    enabled: Boolean,
    first: Boolean,
    last: Boolean,
    onOpen: (ExpenseDomain) -> Unit,
    onOpenPayment: (SettlementDomain) -> Unit,
    onEditExpense: (ExpenseDomain) -> Unit,
    onDeleteExpense: (ExpenseDomain) -> Unit,
    onEditSettlement: (SettlementDomain) -> Unit,
    onDeleteSettlement: (SettlementDomain) -> Unit,
) {
    when (item) {
        is ExpenseHistoryItemUi.Purchase ->
            HistoryCard(
                title = item.expense.title,
                subtitle =
                    stringResource(
                        Res.string.history_paid_on,
                        item.payerName ?: stringResource(Res.string.former_member),
                        calendarDateText(item.calendarDate),
                    ),
                amount = item.amount,
                glyph = if (item.expense.recurring) Glyph.REPEAT else Glyph.MONEY,
                person =
                    HistoryPerson(
                        id = item.expense.paidBy,
                        name = item.payerName.orEmpty(),
                        avatarPath = item.payerAvatarPath,
                    ),
                emphasis = MaterialTheme.colorScheme.primary,
                amountColor = MaterialTheme.colorScheme.onSurface,
                canManage = item.canManage,
                enabled = enabled,
                first = first,
                last = last,
                tag = "expense_actions_${item.expense.id}",
                cardTag = "expense_card_${item.expense.id}",
                onOpen = { onOpen(item.expense) },
                onEdit = { onEditExpense(item.expense) },
                onDelete = { onDeleteExpense(item.expense) },
            )
        is ExpenseHistoryItemUi.Repayment ->
            HistoryCard(
                title =
                    paymentTitle(
                        fromName = item.fromName,
                        toName = item.toName,
                        isUserSender = item.isUserSender,
                        isUserRecipient = item.isUserRecipient,
                    ),
                subtitle = calendarDateText(item.calendarDate),
                amount = item.amount,
                glyph = Glyph.CHECK,
                emphasis =
                    if (item.isUserSender) MaterialTheme.colorScheme.error
                    else if (item.isUserRecipient) LoomColors.positive
                    else MaterialTheme.colorScheme.primary,
                canManage = item.canManage,
                enabled = enabled,
                first = first,
                last = last,
                tag = "payment_actions_${item.settlement.id}",
                cardTag = "payment_card_${item.settlement.id}",
                onOpen = { onOpenPayment(item.settlement) },
                onEdit = { onEditSettlement(item.settlement) },
                onDelete = { onDeleteSettlement(item.settlement) },
            )
    }
}

/** Who a record is about, shown as a small photo before its subtitle. */
private data class HistoryPerson(val id: String, val name: String, val avatarPath: String?)

/** One compact geometry and anchored overflow for both kinds of ledger record. */
@Composable
private fun HistoryCard(
    title: String,
    subtitle: String,
    amount: String,
    glyph: Glyph,
    emphasis: Color,
    canManage: Boolean,
    enabled: Boolean,
    first: Boolean,
    last: Boolean,
    tag: String,
    cardTag: String,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    amountColor: Color = emphasis,
    person: HistoryPerson? = null,
) {
    Surface(
        onClick = onOpen,
        enabled = enabled,
        shape = cardSegmentShape(first = first, last = last),
        color = Color.Transparent,
        modifier =
            Modifier.fillMaxWidth()
                .cardSegment(first = first, last = last, gap = HistoryRowGap)
                .testTag(cardTag),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(LoomSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
        ) {
            IconTile(glyph = glyph, size = EntryIconSize, tint = emphasis)
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(amount, style = MaterialTheme.typography.titleMedium, color = amountColor)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    person?.let {
                        MemberAvatar(
                            memberId = it.id,
                            initials = it.name.trim().take(1).uppercase(),
                            picturePath = it.avatarPath,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (canManage)
                ActionMenu(
                    modifier = Modifier.testTag(tag),
                    items =
                        listOf(
                            ActionMenuItem(
                                text = TextResource.Resource(Res.string.history_edit),
                                onClick = onEdit,
                                enabled = enabled,
                            ),
                            ActionMenuItem(
                                text = TextResource.Resource(Res.string.detail_delete),
                                onClick = onDelete,
                                enabled = enabled,
                                destructive = true,
                            ),
                        ),
                )
        }
    }
}
