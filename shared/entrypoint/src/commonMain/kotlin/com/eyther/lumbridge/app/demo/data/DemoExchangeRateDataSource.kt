package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExchangeRateDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError

/** Fixed rates for the preview, so trips abroad convert without a network call. */
class DemoExchangeRateDataSource : ExchangeRateDataSource {
    private val perEuro = mapOf("EUR" to 1.0, "GBP" to 0.8661, "USD" to 1.0412, "CHF" to 0.9384)

    override suspend fun latestRate(from: String, to: String): AppResult<String> {
        val fromPerEuro = perEuro[from] ?: return AppResult.Failure(ExpenseError.RateUnavailable())
        val toPerEuro = perEuro[to] ?: return AppResult.Failure(ExpenseError.RateUnavailable())
        val micros = kotlin.math.round(toPerEuro / fromPerEuro * 1_000_000).toLong()
        val fraction = (micros % 1_000_000).toString().padStart(6, '0').trimEnd('0')
        return AppResult.Success("${micros / 1_000_000}" + if (fraction.isEmpty()) "" else ".$fraction")
    }
}
