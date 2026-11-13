package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import kotlin.test.*

class MoneyDomainTest {
    @Test
    fun parsesWithoutFloatingPoint() {
        assertEquals(123, MoneyDomain.parse("1.23", "EUR").minorUnits)
        assertEquals(123, MoneyDomain.parse("1,23", "EUR").minorUnits)
        assertEquals(10, MoneyDomain.parse("0.10", "EUR").minorUnits)
    }

    @Test
    fun rejectsInvalidAndOverflowAmounts() {
        listOf("0", "-1", "1.001", "NaN", "1e3", "9999999999999999999999999999").forEach {
            assertFailsWith<IllegalArgumentException> { MoneyDomain.parse(it, "EUR") }
        }
    }

    @Test
    fun formattingDoesNotOverflowForLongMin() {
        assertEquals("−€92233720368547758.08", MoneyDomain(Long.MIN_VALUE, "EUR").formatted())
    }

    @Test
    fun currencyCannotSilentlyUseTheWrongExponent() {
        assertFailsWith<IllegalArgumentException> { MoneyDomain(100, "JPY") }
    }
}
