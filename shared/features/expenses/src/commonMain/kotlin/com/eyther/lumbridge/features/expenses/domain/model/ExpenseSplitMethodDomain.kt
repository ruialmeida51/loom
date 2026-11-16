package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.core.domain.errors.AppError

enum class ExpenseSplitMethodDomain {
    EQUAL,
    SHARES,
    AMOUNTS,
    PERCENTAGES;

    /** Step by one share, percentage point or currency unit, preserving amount precision. */
    fun steppedInput(value: String, currency: String, increase: Boolean): String {
        if (this == EQUAL) return value
        val current =
            if (this == AMOUNTS) {
                try {
                    MoneyDomain.parseShare(text = value, currency = currency).minorUnits
                } catch (_: AppError) {
                    if (value.isBlank()) 0L else return value
                }
            } else if (value.isBlank()) 0L else value.trim().toLongOrNull() ?: return value
        if (current < 0L || (!increase && this == SHARES && current <= 1L)) return value
        val step = if (this == AMOUNTS) 100L else 1L
        val maximum =
            when (this) {
                AMOUNTS -> MoneyDomain.MAX_MINOR_UNITS
                PERCENTAGES -> 100L
                else -> 1_000_000L
            }
        val adjusted =
            (current.coerceIn(0L, maximum) + if (increase) step else -step).coerceIn(
                if (this == SHARES) 1L else 0L,
                maximum,
            )
        return if (this == AMOUNTS) MoneyDomain(adjusted, currency).inputText()
        else adjusted.toString()
    }
}
