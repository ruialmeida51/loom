package com.eyther.lumbridge.features.expenses.presentation

import com.eyther.lumbridge.features.expenses.data.fixtures.ExpenseFormFixture
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.LocalDate

class BalancePresentationTest {
    @Test
    fun theLedgerListsRecordsInTheOrderTheyWereAddedNewestFirst() {
        val fixture = ExpenseFormFixture()
        fun expense(id: String, occurredOn: String, createdAt: String) =
            ExpenseDomain(
                id = id,
                title = id,
                amountMinor = 100,
                paidBy = "you",
                category = "Other",
                occurredOn = LocalDate.parse(occurredOn),
                shares = mapOf("you" to 100L),
                createdAt = createdAt,
            )
        val state =
            ExpensesState(
                userId = "you",
                account = fixture.account,
                members = fixture.members,
                expenses =
                    listOf(
                        expense("first", "2026-11-20", "2026-11-20T09:00:00Z"),
                        expense("backdated", "2026-11-01", "2026-11-20T11:00:00Z"),
                        expense("second", "2026-11-20", "2026-11-20T10:00:00Z"),
                    ),
                settlements =
                    listOf(
                        SettlementDomain(
                            id = "paid",
                            from = "alex",
                            to = "you",
                            amountMinor = 100,
                            occurredOn = LocalDate.parse("2026-11-19"),
                            createdAt = "2026-11-20T12:00:00Z",
                        ),
                    ),
            )
        assertEquals(
            listOf("settlement-paid", "expense-backdated", "expense-second", "expense-first"),
            state.history.map { it.key },
        )
    }

    @Test
    fun oneTransferShowsTheCounterpartyAndKeepsTheActualPaymentDirection() {
        val fixture = ExpenseFormFixture()
        val state =
            ExpensesState(
                userId = "you",
                account = fixture.account,
                members = fixture.members,
                balances = mapOf("you" to 1875L, "alex" to -1875L),
                loading = false,
            )
        assertFalse(
            state.showPersonalSummary,
            "A single personal transfer already contains the total",
        )
        val repayment = state.suggestedRepayments.single()
        assertEquals("alex", repayment.settlement.from)
        assertEquals("you", repayment.settlement.to)
        assertEquals(1875L, repayment.settlement.amountMinor)
        assertTrue(repayment.isCurrentUserRecipient)
        assertTrue(repayment.canRecord)
        val busyRepayment = state.copy(busy = true).suggestedRepayments.single()
        assertTrue(busyRepayment.canRecord, "Pending writes must not remove the row's action label")
        assertFalse(busyRepayment.recordEnabled)
        assertEquals(repayment.amount, busyRepayment.amount)
    }

    @Test
    fun severalCounterpartiesKeepAnAggregatePersonalPosition() {
        val fixture = ExpenseFormFixture()
        val state =
            ExpensesState(
                userId = "you",
                account = fixture.account,
                members = fixture.members,
                balances = mapOf("you" to 3000L, "alex" to -1000L, "sam" to -2000L),
            )
        assertTrue(state.showPersonalSummary)
        assertEquals(3000L, state.ownBalance.money.minorUnits)
        assertEquals(2, state.suggestedRepayments.size)
        assertTrue(state.suggestedRepayments.all { it.settlement.to == "you" })
    }

    @Test
    fun aSettledUserSeesTheirStatusWithoutAnActionToRecordOtherPeoplesPayment() {
        val fixture = ExpenseFormFixture()
        val state =
            ExpensesState(
                userId = "you",
                account = fixture.account,
                members = fixture.members,
                balances = mapOf("you" to 0L, "alex" to 1000L, "sam" to -1000L),
            )
        assertTrue(state.showPersonalSummary)
        assertTrue(state.ownBalance.isSettled)
        assertFalse(state.suggestedRepayments.single().canRecord)
    }
}
