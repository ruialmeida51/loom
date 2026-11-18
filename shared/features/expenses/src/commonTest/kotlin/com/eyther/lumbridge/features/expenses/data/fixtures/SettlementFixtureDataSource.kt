package com.eyther.lumbridge.features.expenses.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.coroutines.flow.Flow

class SettlementFixtureDataSource : ExpenseDataSource {
    var saves = 0
    var removals = 0

    override fun streamExpenses(accountId: String): Flow<AppResult<List<ExpenseDomain>>> =
        error("Not used")

    override fun streamSettlements(accountId: String): Flow<AppResult<List<SettlementDomain>>> =
        error("Not used")

    override suspend fun getExpense(id: String, accountId: String): AppResult<ExpenseDomain> =
        error("Not used")

    override suspend fun saveExpense(expense: ExpenseDomain): AppResult<Unit> = error("Not used")

    override suspend fun deleteExpense(expense: ExpenseDomain): AppResult<Unit> = error("Not used")

    override suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit> =
        error("Not used")

    override suspend fun saveSettlement(settlement: SettlementDomain): AppResult<SettlementDomain> {
        saves++
        return AppResult.Success(settlement.copy(version = settlement.version + 1))
    }

    override suspend fun removeSettlement(settlement: SettlementDomain): AppResult<Unit> {
        removals++
        return AppResult.Success(Unit)
    }
}
