package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain

class GetSettlementUseCase(private val expenseDataSource: ExpenseDataSource) {
    suspend operator fun invoke(id: String, accountId: String): AppResult<SettlementDomain> =
        expenseDataSource.getSettlement(id = id, accountId = accountId)
}
