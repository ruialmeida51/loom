package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitMethodDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class SplitStepperTest {
    @Test
    fun currencySteppingRetainsCentsAndAcceptsCommaInput() {
        val method = ExpenseSplitMethodDomain.AMOUNTS
        assertEquals("2.25", method.steppedInput(value = "1,25", currency = "EUR", increase = true))
        assertEquals(
            "0.25",
            method.steppedInput(value = "1.25", currency = "EUR", increase = false),
        )
        assertEquals(
            "0.00",
            method.steppedInput(value = "0.25", currency = "EUR", increase = false),
        )
    }

    @Test
    fun shareAndPercentageBoundsCannotOverflow() {
        assertEquals(
            "1",
            ExpenseSplitMethodDomain.SHARES.steppedInput(
                value = "1",
                currency = "EUR",
                increase = false,
            ),
        )
        assertEquals(
            "100",
            ExpenseSplitMethodDomain.PERCENTAGES.steppedInput(
                value = "100",
                currency = "EUR",
                increase = true,
            ),
        )
        assertEquals(
            "0",
            ExpenseSplitMethodDomain.PERCENTAGES.steppedInput(
                value = "",
                currency = "EUR",
                increase = false,
            ),
        )
    }

    @Test
    fun steppingDoesNotEraseAnInvalidTextDraft() {
        assertEquals(
            "12.345",
            ExpenseSplitMethodDomain.AMOUNTS.steppedInput(
                value = "12.345",
                currency = "EUR",
                increase = true,
            ),
        )
        assertEquals(
            "no",
            ExpenseSplitMethodDomain.SHARES.steppedInput(
                value = "no",
                currency = "EUR",
                increase = false,
            ),
        )
    }
}
