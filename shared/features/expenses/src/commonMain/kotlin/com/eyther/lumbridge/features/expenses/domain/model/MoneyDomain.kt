package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.household.domain.model.HouseholdCurrencyDomain

/** Money is stored in minor units. V1 households choose a supported two-decimal currency. */
data class MoneyDomain(val minorUnits: Long, val currency: String) {

    init {
        requireDomain(currency in HouseholdCurrencyDomain.supportedCodes) {
            ExpenseError.UnsupportedCurrency()
        }
    }

    fun formatted(): String {
        val negative = minorUnits < 0
        return (if (negative) "−" else "") + formattedMagnitude()
    }

    /** Uses the decimal representation instead of negation, which would overflow for Long.MIN_VALUE. */
    fun formattedMagnitude(): String {
        val digits = minorUnits.toString().removePrefix("-").padStart(3, '0')
        val symbol =
            when (currency) {
                "EUR" -> "€"
                "GBP" -> "£"
                "USD" -> "$"
                else -> "$currency "
            }
        return symbol + digits.dropLast(2) + "." + digits.takeLast(2)
    }

    companion object {
        const val MAX_MINOR_UNITS = 100_000_000_000L

        fun parseShare(text: String, currency: String): MoneyDomain {
            if (text.trim() in setOf("0", "0.00", "0,00")) {
                return MoneyDomain(minorUnits = 0, currency = currency)
            }
            return parse(text = text, currency = currency)
        }

        fun parse(text: String, currency: String): MoneyDomain {
            val clean = text.trim().replace(',', '.')
            requireDomain(Regex("[0-9]+(?:[.][0-9]{1,2})?").matches(clean)) {
                ExpenseError.InvalidAmountPrecision()
            }
            val pieces = clean.split('.')
            val whole =
                pieces.first().toLongOrNull() ?: throw ExpenseError.AmountTooLarge()
            requireDomain(whole <= MAX_MINOR_UNITS / 100) { ExpenseError.AmountTooLarge() }
            val minor = whole * 100 + pieces.getOrElse(1) { "" }.padEnd(2, '0').toLong()
            requireDomain(minor in 1..MAX_MINOR_UNITS) {
                ExpenseError.AmountNotPositive()
            }
            return MoneyDomain(minorUnits = minor, currency = currency)
        }
    }
}
