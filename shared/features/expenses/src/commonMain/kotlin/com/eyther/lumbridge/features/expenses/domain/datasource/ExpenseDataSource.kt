package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.coroutines.flow.Flow

interface ExpenseDataSource {
    fun streamExpenses(accountId: String = ""): Flow<AppResult<List<ExpenseDomain>>>

    fun streamSettlements(accountId: String = ""): Flow<AppResult<List<SettlementDomain>>>

    suspend fun saveExpense(expense: ExpenseDomain): AppResult<Unit>

    suspend fun getExpense(id: String, accountId: String): AppResult<ExpenseDomain>

    suspend fun deleteExpense(expense: ExpenseDomain): AppResult<Unit>

    suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit>
}
