package com.eyther.lumbridge.features.expenses.presentation

import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseFormState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class InlineSplitTest {
    private val state =
        ExpenseFormState(
            userId = "you",
            amount = "90,00",
            split = ExpenseSplitSelectionDomain(ExpenseSplitMethodDomain.EQUAL, setOf("you", "alex", "sam")),
        )

    @Test
    fun anEqualSplitShowsEachPersonsShareAsPeopleAreAddedOrRemoved() {
        assertEquals("€30.00", state.equalShare)
        assertEquals(
            "€45.00",
            state.copy(split = state.split.copy(participants = setOf("you", "alex"))).equalShare,
        )
        assertEquals(
            "€33.33",
            state.copy(amount = "100", split = state.split).equalShare,
        )
    }

    @Test
    fun noShareShowsWithoutAnAmountOrForUnevenSplits() {
        assertNull(state.copy(amount = "").equalShare)
        assertNull(
            state.copy(split = state.split.copy(method = ExpenseSplitMethodDomain.AMOUNTS)).equalShare,
        )
    }
}
