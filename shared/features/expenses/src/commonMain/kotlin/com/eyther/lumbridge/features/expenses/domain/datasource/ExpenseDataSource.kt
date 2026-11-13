package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.coroutines.flow.Flow

interface ExpenseDataSource {
    fun streamExpenses(): Flow<AppResult<List<ExpenseDomain>>>

    fun streamSettlements(): Flow<AppResult<List<SettlementDomain>>>

    suspend fun addExpense(expense: ExpenseDomain): AppResult<Unit>

    suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit>
}
