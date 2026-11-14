package com.eyther.lumbridge.features.insights.domain.model

import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.insights.domain.errors.InsightsError
import kotlinx.datetime.LocalDate

data class MonthlyStatementDomain(
    val month: String,
    val totalMinor: Long,
    val paidMinor: Long,
    val shareMinor: Long,
    val openingBalanceMinor: Long,
    val closingBalanceMinor: Long,
    val categories: Map<String, Long>,
    val expenses: List<ExpenseDomain>,
) {

    fun csv(
        currency: String,
        userId: String,
        summaryHeader: String =
            "month,currency,household_total_minor,you_paid_minor,your_share_minor,opening_balance_minor,closing_balance_minor",
        expenseHeader: String = "date,description,category,amount_minor,your_share_minor",
    ): String {
        fun quote(value: String): String {
            val safe =
                if (value.firstOrNull() in listOf('=', '+', '-', '@', '\t', '\r')) "'" + value
                else value
            return "\"" + safe.replace("\"", "\"\"") + "\""
        }
        val lines =
            mutableListOf(
                summaryHeader,
            )
        lines +=
            "$month,$currency,$totalMinor,$paidMinor,$shareMinor,$openingBalanceMinor,$closingBalanceMinor"
        lines += ""
        lines += expenseHeader
        expenses.forEach { expense ->
            lines +=
                "${expense.occurredOn},${quote(expense.title)},${quote(expense.category)},${expense.amountMinor},${expense.shares[userId]?:0}"
        }
        return lines.joinToString("\r\n")
    }

    companion object {
        fun build(
            month: String,
            userId: String,
            expenses: List<ExpenseDomain>,
            settlements: List<SettlementDomain>,
        ): MonthlyStatementDomain {
            try {
                if (!Regex("[0-9]{4}-[0-9]{2}").matches(month)) throw InsightsError.InvalidMonth()
                LocalDate.parse("$month-01")
            } catch (error: IllegalArgumentException) {
                throw InsightsError.InvalidMonth(cause = error)
            }
            val selected = expenses.filter { it.occurredOn.startsWith(month) }
            val beforeExpenses = expenses.filter { it.occurredOn.take(7) < month }
            val throughExpenses = expenses.filter { it.occurredOn.take(7) <= month }
            val beforeSettlements = settlements.filter { it.occurredOn.take(7) < month }
            val throughSettlements = settlements.filter { it.occurredOn.take(7) <= month }
            return MonthlyStatementDomain(
                month = month,
                totalMinor = selected.sumOf { it.amountMinor },
                paidMinor = selected.filter { it.paidBy == userId }.sumOf { it.amountMinor },
                shareMinor = selected.sumOf { it.shares[userId] ?: 0 },
                openingBalanceMinor = HouseholdLedgerDomain.balances(beforeExpenses, beforeSettlements)[userId] ?: 0,
                closingBalanceMinor = HouseholdLedgerDomain.balances(throughExpenses, throughSettlements)[userId] ?: 0,
                categories = selected.groupBy { it.category }.mapValues { (_, values) ->
                    values.sumOf { it.amountMinor }
                },
                expenses = selected,
            )
        }
    }
}
