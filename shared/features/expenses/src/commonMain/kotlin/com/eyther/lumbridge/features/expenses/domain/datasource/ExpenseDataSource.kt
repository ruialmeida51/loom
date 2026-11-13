package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.coroutines.flow.Flow

interface ExpenseDataSource {
    fun observeExpenses(): Flow<List<ExpenseDomain>>

    fun observeSettlements(): Flow<List<SettlementDomain>>

    suspend fun add(expense: ExpenseDomain): AppResult<Unit>

    suspend fun settle(settlement: SettlementDomain): AppResult<Unit>
}
