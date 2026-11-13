package com.eyther.lumbridge.features.insights.domain

import com.eyther.lumbridge.features.expenses.domain.model.*
import com.eyther.lumbridge.features.insights.domain.model.MonthlyStatementDomain
import kotlin.test.*

class MonthlyStatementTest {
    @Test
    fun monthBoundaryAndRepaymentAreAccountedSeparately() {
        val members = listOf("a", "b", "c")
        val expenses =
            listOf(
                ExpenseDomain(
                    "jan",
                    "Food",
                    6000,
                    "a",
                    "Groceries",
                    "2026-01-31",
                    ExpenseSplitDomain.equal(6000, members),
                ),
                ExpenseDomain(
                    "feb",
                    "Internet",
                    3000,
                    "b",
                    "Bills",
                    "2026-02-01",
                    ExpenseSplitDomain.equal(3000, members),
                ),
            )
        val payments = listOf(SettlementDomain("payment", "b", "a", 2000, "2026-02-02"))
        val statement = MonthlyStatementDomain.build("2026-02", "a", expenses, payments)
        assertEquals(3000, statement.totalMinor)
        assertEquals(0, statement.paidMinor)
        assertEquals(1000, statement.shareMinor)
        assertEquals(4000, statement.openingBalanceMinor)
        assertEquals(1000, statement.closingBalanceMinor)
        assertEquals(mapOf("Bills" to 3000L), statement.categories)
    }

    @Test
    fun csvQuotesCellsAndGuardsSpreadsheetFormulas() {
        val expense =
            ExpenseDomain("id", "=SUM(1,2)", 100, "a", "Other", "2026-02-01", mapOf("a" to 100L))
        val csv =
            MonthlyStatementDomain.build("2026-02", "a", listOf(expense), emptyList())
                .csv("EUR", "a")
        assertTrue(csv.contains("'=SUM(1,2)"))
        assertTrue(csv.contains("2026-02-01"))
    }
}
