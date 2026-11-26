package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerCursorDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerPageDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.coroutines.flow.Flow

interface ExpenseDataSource {
    /** Every expense of the account, however many; for statements that need full history. */
    fun streamExpenses(accountId: String = ""): Flow<AppResult<List<ExpenseDomain>>>

    /** Every repayment of the account, however many; for statements that need full history. */
    fun streamSettlements(accountId: String = ""): Flow<AppResult<List<SettlementDomain>>>

    /** Everyone's balance in the account, added up by the server over its whole history. */
    fun streamBalances(accountId: String = ""): Flow<AppResult<Map<String, Long>>>

    /** The account's newest [limit] expenses and repayments; reloads after this app's writes. */
    fun streamLedgerPage(accountId: String, limit: Int): Flow<AppResult<LedgerPageDomain>>

    /** Up to [limit] expenses and repayments entered before [after], newest first. */
    suspend fun getLedgerPage(
        accountId: String,
        after: LedgerCursorDomain,
        limit: Int,
    ): AppResult<LedgerPageDomain>

    suspend fun saveExpense(expense: ExpenseDomain): AppResult<Unit>

    suspend fun getExpense(id: String, accountId: String): AppResult<ExpenseDomain>

    suspend fun getSettlement(id: String, accountId: String): AppResult<SettlementDomain>

    suspend fun deleteExpense(expense: ExpenseDomain): AppResult<Unit>

    suspend fun recordSettlement(settlement: SettlementDomain): AppResult<Unit>

    suspend fun saveSettlement(settlement: SettlementDomain): AppResult<SettlementDomain>

    suspend fun removeSettlement(settlement: SettlementDomain): AppResult<Unit>
}
