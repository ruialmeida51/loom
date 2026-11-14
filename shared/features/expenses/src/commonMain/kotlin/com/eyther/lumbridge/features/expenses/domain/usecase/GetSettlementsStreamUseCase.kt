package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.coroutines.flow.Flow

class GetSettlementsStreamUseCase(
    private val expenseDataSource: ExpenseDataSource,
) {

    operator fun invoke(): Flow<AppResult<List<SettlementDomain>>> = expenseDataSource.streamSettlements()
}
