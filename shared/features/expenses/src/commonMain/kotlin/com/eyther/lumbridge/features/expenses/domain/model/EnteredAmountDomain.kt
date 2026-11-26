package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError

/**
 * An amount as typed on the expense form: [text] in [currency], which may differ from the
 * account's [accountCurrency], converted at [rate] when it does.
 */
data class EnteredAmountDomain(
    val text: String,
    val currency: String,
    val accountCurrency: String,
    val rate: String?,
) {
    val isForeign: Boolean = currency != accountCurrency

    /** The original amount to keep, or null when it was paid in the account's currency. */
    fun original(): OriginalAmountDomain? {
        if (!isForeign) return null
        val usableRate = rate ?: throw ExpenseError.RateUnavailable()
        return OriginalAmountDomain(
            currency = currency,
            amountMinor = MoneyDomain.parse(text = text, currency = currency).minorUnits,
            rate = usableRate,
        )
    }

    /** What the expense costs in the account's currency, which balances and shares use. */
    fun accountMinor(): Long =
        original()?.convertedMinor()
            ?: MoneyDomain.parse(text = text, currency = accountCurrency).minorUnits
}
