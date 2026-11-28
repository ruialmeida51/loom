package com.eyther.lumbridge.features.expenses.presentation.mapper

import com.eyther.lumbridge.features.account.domain.model.PaymentHandlesDomain
import com.eyther.lumbridge.features.expenses.domain.model.AccountSpendingDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_even
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_everyone
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_footer
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_owed
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_owes
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_paid
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_pay_me
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_settle_up
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_settled
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_spent
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_summary_title
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_title
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.generated.resources.pay_mbway
import com.eyther.lumbridge.features.expenses.generated.resources.pay_paypal
import com.eyther.lumbridge.features.expenses.generated.resources.pay_revolut
import com.eyther.lumbridge.features.expenses.presentation.components.formatPhone
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

private const val Rule = "━━━━━━━━━━━━"

/** The emoji that stands for an account's icon in shared text. */
internal fun ExpenseAccountIconDomain.shareEmoji(): String =
    when (this) {
        ExpenseAccountIconDomain.HOME -> "🏠"
        ExpenseAccountIconDomain.TRAVEL -> "✈️"
        ExpenseAccountIconDomain.EVENT -> "🎉"
        ExpenseAccountIconDomain.GIFT -> "🎁"
        ExpenseAccountIconDomain.FOOD -> "🍽️"
        ExpenseAccountIconDomain.NATURE -> "🌿"
        ExpenseAccountIconDomain.PETS -> "🐾"
        ExpenseAccountIconDomain.HEALTH -> "💊"
    }

/**
 * Where [account] stands, as text for the household chat: who pays whom to settle up, where
 * everyone stands, and, when someone owes you, how to pay you. People are named, the sender
 * included, since "you" would mean whoever reads it. [balances] are minor units per person, in
 * the order the balances view lists them. A trip's [spending] adds what was spent and who paid,
 * so the message reads as the trip's summary.
 */
suspend fun balanceShareText(
    account: ExpenseAccountDomain,
    names: Map<String, String>,
    balances: List<Pair<String, Long>>,
    suggestions: List<SuggestedSettlementDomain>,
    userId: String,
    ownHandles: PaymentHandlesDomain?,
    spending: AccountSpendingDomain? = null,
): String {
    val former = getString(Res.string.former_member)
    fun name(id: String): String = names[id]?.ifBlank { null } ?: former
    fun money(minor: Long): String = MoneyDomain(minor, account.currency).formattedMagnitude()
    return buildString {
        val summary = spending?.takeIf { !account.isDefault && it.totalMinor > 0 }
        appendLine(
            "${account.displayIcon.shareEmoji()} " +
                getString(
                    if (summary != null) Res.string.balance_share_summary_title
                    else Res.string.balance_share_title,
                    account.name,
                ),
        )
        appendLine(Rule)
        summary?.let {
            appendLine(
                getPluralString(
                    Res.plurals.balance_share_spent,
                    it.expenseCount.toInt(),
                    money(it.totalMinor),
                    it.expenseCount.toInt(),
                ),
            )
            appendLine(getString(Res.string.balance_share_paid))
            it.paidByPerson.entries
                .sortedByDescending { (_, paid) -> paid }
                .forEach { (id, paid) -> appendLine("• ${name(id)} ${money(paid)}") }
            appendLine()
        }
        if (suggestions.isEmpty()) appendLine(getString(Res.string.balance_share_settled))
        else {
            appendLine(getString(Res.string.balance_share_settle_up))
            suggestions.forEach { appendLine("• ${name(it.from)} → ${name(it.to)}: ${money(it.amountMinor)}") }
            appendLine()
            appendLine(getString(Res.string.balance_share_everyone))
            balances.forEach { (id, minor) ->
                val line =
                    when {
                        minor > 0 -> getString(Res.string.balance_share_owed, name(id), money(minor))
                        minor < 0 -> getString(Res.string.balance_share_owes, name(id), money(minor))
                        else -> getString(Res.string.balance_share_even, name(id))
                    }
                appendLine("• $line")
            }
        }
        // Only worth saying when the readers have someone to pay: you.
        ownHandles
            ?.takeIf { !it.isEmpty && suggestions.any { s -> s.to == userId } }
            ?.let { handles ->
                appendLine()
                appendLine(getString(Res.string.balance_share_pay_me))
                handles.mbwayPhone?.let { appendLine("${getString(Res.string.pay_mbway)}: ${formatPhone(it)}") }
                handles.revolutLink()?.let {
                    appendLine("${getString(Res.string.pay_revolut)}: ${it.removePrefix("https://")}")
                }
                handles.paypalProfileLink()?.let {
                    appendLine("${getString(Res.string.pay_paypal)}: ${it.removePrefix("https://")}")
                }
            }
        appendLine()
        append(getString(Res.string.balance_share_footer))
    }
}
