package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.SettlementError
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain

class RemoveSettlementUseCase(private val expenseDataSource: ExpenseDataSource) {
    suspend operator fun invoke(settlement: SettlementDomain): AppResult<Unit> = safeCall {
        requireDomain(settlement.version > 0) { SettlementError.Changed() }
        expenseDataSource.removeSettlement(settlement = settlement).getOrThrow()
    }
}
