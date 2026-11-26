package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.EnteredAmountDomain
import com.eyther.lumbridge.features.expenses.domain.model.OriginalAmountDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TripCurrencyTest {
    @Test
    fun conversionRoundsHalfUpToTheCentLikeTheServer() {
        assertEquals(5542L, OriginalAmountDomain("GBP", 4800, "1.1546").convertedMinor())
        assertEquals(1L, OriginalAmountDomain("HUF", 400, "0.0025").convertedMinor())
        assertEquals(2L, OriginalAmountDomain("USD", 3, "0.5").convertedMinor())
    }

    @Test
    fun ratesAreNormalisedOrRejected() {
        assertEquals("1.1546", OriginalAmountDomain.normalizeRate(" 1,154600 "))
        assertEquals("2", OriginalAmountDomain.normalizeRate("2.0"))
        assertEquals("0.0004", OriginalAmountDomain.normalizeRate("0.0004"))
        assertNull(OriginalAmountDomain.normalizeRate("0"))
        assertNull(OriginalAmountDomain.normalizeRate("abc"))
        assertNull(OriginalAmountDomain.normalizeRate("1.1234567"))
        assertNull(OriginalAmountDomain.normalizeRate("5000"))
    }

    @Test
    fun anAmountInTheAccountsCurrencyNeedsNoRate() {
        val local = EnteredAmountDomain(text = "12.50", currency = "EUR", accountCurrency = "EUR", rate = null)
        assertEquals(1250L, local.accountMinor())
        assertNull(local.original())
        val abroad = EnteredAmountDomain(text = "48", currency = "GBP", accountCurrency = "EUR", rate = "1.1546")
        assertEquals(5542L, abroad.accountMinor())
        assertEquals(OriginalAmountDomain("GBP", 4800, "1.1546"), abroad.original())
        assertFailsWith<ExpenseError.RateUnavailable> {
            abroad.copy(rate = null).accountMinor()
        }
    }
}
