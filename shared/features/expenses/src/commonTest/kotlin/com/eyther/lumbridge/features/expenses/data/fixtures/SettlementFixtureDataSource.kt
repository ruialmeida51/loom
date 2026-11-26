package com.eyther.lumbridge.features.expenses.data.fixtures

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerCursorDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerPageDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow

class SettlementFixtureDataSource : ExpenseDataSource {
    var saves = 0
    var removals = 0
    var records = 0
    var payment: SettlementDomain? = null
    var mutationError: AppError? = null
    var mutationGate: CompletableDeferred<Unit>? = null
    val savedPayments = mutableListOf<SettlementDomain>()
    val removedPayments = mutableListOf<SettlementDomain>()

    override fun streamExpenses(accountId: String): Flow<AppResult<List<ExpenseDomain>>> =
        error("Not used")

    override fun streamSettlements(accountId: String): Flow<AppResult<List<SettlementDomain>>> =
        error("Not used")

    override fun streamBalances(accountId: String): Flow<AppResult<Map<String, Long>>> =
        error("Not used")

    override fun streamLedgerPage(
        accountId: String,
        limit: Int,
    ): Flow<AppResult<LedgerPageDomain>> = error("Not used")

    override suspend fun getLedgerPage(
        accountId: String,
        after: LedgerCursorDomain,
        limit: Int,
    ): AppResult<LedgerPageDomain> = error("Not used")

    override suspend fun getSettlement(id: String, accountId: String): AppResult<SettlementDomain> =
        payment
            ?.takeIf { it.id == id && it.accountId == accountId }
            ?.let {
                AppResult.Success(it)
            } ?: AppResult.Failure(CommonError.NotFound())

    override suspend fun getExpense(id: String, accountId: String): AppResult<ExpenseDomain> =
        error("Not used")

    override suspend fun saveExpense(expense: ExpenseDomain): AppResult<Unit> = error("Not used")

    override suspend fun deleteExpense(expense: ExpenseDomain): AppResult<Unit> = error("Not used")

    override suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit> {
        records++
        savedPayments += settlement
        mutationGate?.await()
        mutationError?.let {
            return AppResult.Failure(it)
        }
        payment = settlement.copy(version = 1)
        return AppResult.Success(Unit)
    }

    override suspend fun saveSettlement(settlement: SettlementDomain): AppResult<SettlementDomain> {
        saves++
        savedPayments += settlement
        mutationGate?.await()
        mutationError?.let {
            return AppResult.Failure(it)
        }
        val saved = settlement.copy(version = settlement.version + 1)
        payment = saved
        return AppResult.Success(saved)
    }

    override suspend fun removeSettlement(settlement: SettlementDomain): AppResult<Unit> {
        removals++
        removedPayments += settlement
        mutationGate?.await()
        mutationError?.let {
            return AppResult.Failure(it)
        }
        payment = null
        return AppResult.Success(Unit)
    }
}
