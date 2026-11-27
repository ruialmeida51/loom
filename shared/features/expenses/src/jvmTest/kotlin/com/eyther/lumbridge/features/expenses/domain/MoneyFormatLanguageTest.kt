package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MoneyFormatLanguageTest {
    private val original = Locale.getDefault()

    @AfterTest
    fun restore() {
        Locale.setDefault(original)
    }

    @Test
    fun englishPutsTheSymbolFirstWithADecimalPoint() {
        Locale.setDefault(Locale.forLanguageTag("en-GB"))
        assertEquals("€26.33", MoneyDomain(minorUnits = 2633, currency = "EUR").formatted())
        assertEquals("−£4.05", MoneyDomain(minorUnits = -405, currency = "GBP").formatted())
    }

    @Test
    fun portuguesePutsTheAmountFirstWithADecimalComma() {
        Locale.setDefault(Locale.forLanguageTag("pt-PT"))
        assertEquals("26,33 €", MoneyDomain(minorUnits = 2633, currency = "EUR").formatted())
        assertEquals("−4,05 £", MoneyDomain(minorUnits = -405, currency = "GBP").formatted())
        assertEquals("1200,00 CHF", MoneyDomain(minorUnits = 120000, currency = "CHF").formatted())
    }
}
