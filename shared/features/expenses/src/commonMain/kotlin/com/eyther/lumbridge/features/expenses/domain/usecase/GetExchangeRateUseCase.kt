package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExchangeRateDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseError
import com.eyther.lumbridge.features.expenses.domain.model.TravelCurrencyDomain

class GetExchangeRateUseCase(private val exchangeRateDataSource: ExchangeRateDataSource) {
    suspend operator fun invoke(from: String, to: String): AppResult<String> =
        when {
            from == to -> AppResult.Success("1")
            !TravelCurrencyDomain.isSupported(from) || !TravelCurrencyDomain.isSupported(to) ->
                AppResult.Failure(ExpenseError.UnsupportedCurrency())
            else -> exchangeRateDataSource.latestRate(from = from, to = to)
        }
}
