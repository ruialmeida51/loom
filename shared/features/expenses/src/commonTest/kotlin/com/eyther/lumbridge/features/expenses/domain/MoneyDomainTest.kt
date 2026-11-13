package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MoneyDomainTest {

    @Test
    fun parsesWithoutFloatingPoint() {
        assertEquals(123, MoneyDomain.parse("1.23", "EUR").minorUnits)
        assertEquals(123, MoneyDomain.parse("1,23", "EUR").minorUnits)
        assertEquals(10, MoneyDomain.parse("0.10", "EUR").minorUnits)
    }

    @Test
    fun rejectsInvalidAndOverflowAmounts() {
        listOf("-1", "1.001", "NaN", "1e3").forEach {
            assertFailsWith<ExpenseError.InvalidAmountPrecision> { MoneyDomain.parse(it, "EUR") }
        }
        assertFailsWith<ExpenseError.AmountNotPositive> { MoneyDomain.parse("0", "EUR") }
        assertFailsWith<ExpenseError.AmountTooLarge> {
            MoneyDomain.parse("9999999999999999999999999999", "EUR")
        }
    }

    @Test
    fun formattingDoesNotOverflowForLongMin() {
        assertEquals("−€92233720368547758.08", MoneyDomain(Long.MIN_VALUE, "EUR").formatted())
    }

    @Test
    fun currencyCannotSilentlyUseTheWrongExponent() {
        assertFailsWith<ExpenseError.UnsupportedCurrency> { MoneyDomain(100, "JPY") }
    }
}
