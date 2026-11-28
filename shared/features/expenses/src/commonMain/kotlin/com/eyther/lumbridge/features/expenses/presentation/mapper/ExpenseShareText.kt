package com.eyther.lumbridge.features.expenses.presentation.mapper

import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.account_everyday
import com.eyther.lumbridge.features.expenses.generated.resources.balance_share_footer
import com.eyther.lumbridge.features.expenses.generated.resources.expense_share_paid_by
import com.eyther.lumbridge.features.expenses.generated.resources.expense_share_split
import com.eyther.lumbridge.features.expenses.generated.resources.former_member
import com.eyther.lumbridge.features.expenses.presentation.model.ExpenseDetailsUi
import org.jetbrains.compose.resources.getString

private const val Rule = "━━━━━━━━━━━━"

/**
 * One expense as text for the household chat: what it was, how much and when, who paid and in
 * which account, and each person's share, named. [date] is the expense date as the app shows it.
 */
suspend fun expenseShareText(details: ExpenseDetailsUi, date: String): String {
    val former = getString(Res.string.former_member)
    val account =
        if (details.isEveryday) getString(Res.string.account_everyday) else details.accountName
    return buildString {
        appendLine("🧾 ${details.title}")
        appendLine(Rule)
        appendLine("${details.amount} · $date")
        appendLine(
            getString(Res.string.expense_share_paid_by, details.payerName ?: former, account),
        )
        details.originalAmount?.let { original ->
            appendLine(listOfNotNull(original, details.originalRate).joinToString(" · "))
        }
        if (details.shares.isNotEmpty()) {
            appendLine()
            appendLine(getString(Res.string.expense_share_split))
            details.shares.forEach { share -> appendLine("• ${share.name ?: former} ${share.amount}") }
        }
        appendLine()
        append(getString(Res.string.balance_share_footer))
    }
}
