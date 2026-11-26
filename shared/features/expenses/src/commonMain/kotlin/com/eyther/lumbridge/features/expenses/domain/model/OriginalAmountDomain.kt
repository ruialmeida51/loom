package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError

/**
 * What an expense cost in the currency it was paid in, and the rate that turns it into the
 * account's currency. [rate] is a decimal with up to [RATE_DECIMALS] places, such as "1.1546".
 */
data class OriginalAmountDomain(val currency: String, val amountMinor: Long, val rate: String) {
    /** The rate in millionths, so conversion stays exact in whole numbers. */
    private val rateMicros: Long = parseRateMicros(rate)

    /** The account-currency amount: the original times the rate, rounded half up to a cent. */
    fun convertedMinor(): Long {
        requireDomain(amountMinor in 1..MAXIMUM_ORIGINAL_MINOR) { ExpenseError.AmountTooLarge() }
        return (amountMinor * rateMicros + MICROS / 2) / MICROS
    }

    companion object {
        const val RATE_DECIMALS = 6
        private const val MICROS = 1_000_000L

        /** Keeps amount × rate inside a Long: ten million units at a rate of up to 1000. */
        const val MAXIMUM_ORIGINAL_MINOR = 1_000_000_000L
        private const val MAXIMUM_RATE_MICROS = 1_000L * MICROS

        /** A rate as people type it ("1,1546" or "1.1546"), or null when it is not a usable rate. */
        fun normalizeRate(text: String): String? {
            val clean = text.trim().replace(',', '.')
            if (!Regex("[0-9]{1,4}(?:[.][0-9]{1,$RATE_DECIMALS})?").matches(clean)) return null
            val micros = parseRateMicros(clean)
            if (micros <= 0 || micros > MAXIMUM_RATE_MICROS) return null
            val whole = micros / MICROS
            val fraction = (micros % MICROS).toString().padStart(RATE_DECIMALS, '0').trimEnd('0')
            return if (fraction.isEmpty()) whole.toString() else "$whole.$fraction"
        }

        private fun parseRateMicros(rate: String): Long {
            val pieces = rate.split('.')
            val whole = pieces[0].toLongOrNull() ?: return 0
            val fraction = pieces.getOrElse(1) { "" }.take(RATE_DECIMALS).padEnd(RATE_DECIMALS, '0')
            return whole * MICROS + (fraction.toLongOrNull() ?: 0)
        }
    }
}
