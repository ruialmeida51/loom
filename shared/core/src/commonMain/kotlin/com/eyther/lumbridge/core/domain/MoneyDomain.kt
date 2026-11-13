package com.eyther.lumbridge.core.domain

/** Money is stored in minor units. V1 households choose a supported two-decimal currency. */
data class MoneyDomain(val minorUnits: Long, val currency: String) {
    init {
        require(currency in supportedCurrencies) { "Choose a supported currency." }
    }

    fun formatted(): String {
        val negative = minorUnits < 0
        val digits = minorUnits.toString().removePrefix("-").padStart(3, '0')
        val symbol =
            when (currency) {
                "EUR" -> "€"
                "GBP" -> "£"
                "USD" -> "$"
                else -> "$currency "
            }
        return (if (negative) "−" else "") + symbol + digits.dropLast(2) + "." + digits.takeLast(2)
    }

    companion object {
        val supportedCurrencies = setOf("EUR", "GBP", "USD", "CAD", "AUD")
        const val MAX_MINOR_UNITS = 100_000_000_000L

        fun parse(text: String, currency: String): MoneyDomain {
            val clean = text.trim().replace(',', '.')
            require(Regex("[0-9]+(?:[.][0-9]{1,2})?").matches(clean)) {
                "Enter an amount with at most two decimal places."
            }
            val pieces = clean.split('.')
            val whole =
                requireNotNull(pieces.first().toLongOrNull()) { "That amount is too large." }
            require(whole <= MAX_MINOR_UNITS / 100) { "That amount is too large." }
            val minor = whole * 100 + pieces.getOrElse(1) { "" }.padEnd(2, '0').toLong()
            require(minor in 1..MAX_MINOR_UNITS) { "Enter an amount greater than zero." }
            return MoneyDomain(minor, currency)
        }
    }
}
