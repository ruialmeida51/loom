package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult

interface ExchangeRateDataSource {
    /** Today's rate for 1 [from] in [to], as a decimal such as "1.1546". */
    suspend fun latestRate(from: String, to: String): AppResult<String>
}
