package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitDomain
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class HouseholdLedgerTest {

    private val members = listOf("a", "b", "c")

    private fun expense(total: Long = 6000) =
        ExpenseDomain(
            "one",
            "Groceries",
            total,
            "a",
            "Groceries",
            LocalDate.parse("2026-11-01"),
            ExpenseSplitDomain.equal(total, members),
        )

    @Test
    fun equalSplitsPreserveEveryCentAndIgnoreInputOrder() {
        assertEquals(
            mapOf("a" to 34L, "b" to 33L, "c" to 33L),
            ExpenseSplitDomain.equal(100, members.reversed()),
        )
        assertEquals(1, ExpenseSplitDomain.equal(1, members).values.sum())
        assertEquals(2, ExpenseSplitDomain.equal(1, members).values.count { it == 0L })
    }

    @Test
    fun percentageRoundingUsesLargestRemainder() {
        assertEquals(
            mapOf("a" to 35L, "b" to 33L, "c" to 33L),
            ExpenseSplitDomain.percentages(101, mapOf("a" to 34, "b" to 33, "c" to 33)),
        )
        assertFailsWith<ExpenseError.InvalidPercentages> {
            ExpenseSplitDomain.percentages(100, mapOf("a" to 50, "b" to 49))
        }
    }

    @Test
    fun rejectsDuplicateMembersAndIncorrectTotals() {
        assertFailsWith<ExpenseError.DuplicateParticipants> {
            ExpenseSplitDomain.equal(100, listOf("a", "a"))
        }
        assertFailsWith<ExpenseError.IncorrectSplitTotal> {
            ExpenseSplitDomain.validate(100, mapOf("a" to 99))
        }
        assertFailsWith<ExpenseError.InvalidShare> {
            ExpenseSplitDomain.validate(100, mapOf("a" to -1, "b" to 101))
        }
    }

    @Test
    fun payerIsOwedOnlyOtherPeoplesShares() {
        assertEquals(
            mapOf("a" to 4000L, "b" to -2000L, "c" to -2000L),
            HouseholdLedgerDomain.balances(listOf(expense()), emptyList()),
        )
    }

    @Test
    fun partialRepaymentReducesDebtInCorrectDirection() {
        val balances =
            HouseholdLedgerDomain.balances(
                listOf(expense()),
                listOf(SettlementDomain("payment", "b", "a", 1000, LocalDate.parse("2026-11-02"))),
            )
        assertEquals(mapOf("a" to 3000L, "b" to -1000L, "c" to -2000L), balances)
        assertEquals(0, balances.values.sum())
    }

    @Test
    fun suggestedRepaymentsHaveStableOrderAcrossPlatforms() {
        val balances = linkedMapOf("d" to -200L, "b" to 150L, "c" to -100L, "a" to 150L)
        val expected =
            listOf(
                SuggestedSettlementDomain("c", "a", 100L),
                SuggestedSettlementDomain("d", "a", 50L),
                SuggestedSettlementDomain("d", "b", 150L),
            )
        assertEquals(expected, HouseholdLedgerDomain.suggestions(balances))
        assertEquals(
            expected,
            HouseholdLedgerDomain.suggestions(
                balances.entries.reversed().associate { it.key to it.value },
            ),
        )
    }

    @Test
    fun suggestedRepaymentsReconcileToZero() {
        for (total in 1L..500L) {
            val expenses = listOf(expense(total))
            val balances = HouseholdLedgerDomain.balances(expenses, emptyList())
            val repayments =
                HouseholdLedgerDomain.suggestions(balances).mapIndexed { index, s ->
                    SettlementDomain(index.toString(), s.from, s.to, s.amountMinor, LocalDate.parse("2026-11-02"))
                }
            assertTrue(HouseholdLedgerDomain.balances(expenses, repayments).values.all { it == 0L })
        }
    }
}
