package com.eyther.lumbridge.features.expenses.presentation

import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.presentation.model.BalanceSummaryUi
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.RecurringExpensesState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ExpensePresentationTest {

    @Test
    fun balancesKeepTheirDirectionWithoutNegatingTheStoredAmount() {
        val debt = BalanceSummaryUi(money = MoneyDomain(minorUnits = -1250, currency = "EUR"))
        val extreme = BalanceSummaryUi(money = MoneyDomain(minorUnits = Long.MIN_VALUE, currency = "EUR"))

        assertFalse(debt.isOwed)
        assertEquals("€12.50", debt.amount)
        assertEquals("€92233720368547758.08", extreme.amount)
        assertEquals("−€12.50", debt.money.formatted())
    }

    @Test
    fun scheduleActionsAreRecomputedWhenMembershipOrCancellationChanges() {
        val rule = RecurringExpenseDomain(
            id = "bill",
            title = "Internet",
            amountMinor = 4000,
            paidBy = "payer",
            category = "Bills",
            nextOn = "2026-12-01",
            anchorDay = 1,
            active = true,
            cancelled = false,
            createdBy = "creator",
            version = 1,
            shares = mapOf("payer" to 4000),
        )
        val state = RecurringExpensesState(userId = "other-member", rules = listOf(rule), role = "member")

        assertFalse(state.rows.single().canManage)
        assertTrue(state.copy(role = "owner").rows.single().canManage)
        assertFalse(state.copy(role = "owner", rules = listOf(rule.copy(cancelled = true))).rows.single().canManage)
        assertTrue(state.copy(rules = listOf(rule.copy(active = false))).rows.single().isPaused)
    }
}
