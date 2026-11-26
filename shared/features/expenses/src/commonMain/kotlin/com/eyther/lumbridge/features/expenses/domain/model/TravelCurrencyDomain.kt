package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.features.household.domain.model.HouseholdCurrencyDomain

/**
 * Currencies an expense can be paid in abroad: the household currencies plus the two-decimal
 * currencies the European Central Bank publishes daily rates for. Zero-decimal currencies (yen,
 * won, krónur) are left out because amounts are kept in cents.
 */
object TravelCurrencyDomain {
    val codes: List<String> =
        (HouseholdCurrencyDomain.supportedCodes.sorted() +
                listOf(
                    "BRL", "CHF", "CNY", "CZK", "DKK", "HKD", "HUF", "ILS", "INR", "MXN", "MYR",
                    "NOK", "NZD", "PHP", "PLN", "RON", "SEK", "SGD", "THB", "TRY", "ZAR",
                ))
            .distinct()

    fun isSupported(code: String): Boolean = code in codes
}
