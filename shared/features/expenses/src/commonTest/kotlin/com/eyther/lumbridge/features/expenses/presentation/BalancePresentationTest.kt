package com.eyther.lumbridge.features.expenses.presentation

import com.eyther.lumbridge.features.expenses.data.fixtures.ExpenseFormFixture
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpensesState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BalancePresentationTest {
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
