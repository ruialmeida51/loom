package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.SettlementError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerCursorDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerPageDomain
import com.eyther.lumbridge.features.expenses.domain.model.LedgerRecordDomain
import com.eyther.lumbridge.features.expenses.domain.model.MonthlyRecurrenceDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlin.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class DemoExpenseDataSource(private val store: DemoHouseholdStore) : ExpenseDataSource {

    override fun streamExpenses(accountId: String) =
        store.expenses.map { expenses ->
            AppResult.Success(accountExpenses(expenses = expenses, accountId = accountId))
        }

    override fun streamSettlements(accountId: String) =
        store.settlements.map { settlements ->
            AppResult.Success(accountSettlements(settlements = settlements, accountId = accountId))
        }

    /** Adds the account up in memory, as the server's balances view does. */
    override fun streamBalances(accountId: String): Flow<AppResult<Map<String, Long>>> =
        combine(store.expenses, store.settlements) { expenses, settlements ->
            safeCall {
                HouseholdLedgerDomain.balances(
                    expenses = accountExpenses(expenses = expenses, accountId = accountId),
                    settlements = accountSettlements(settlements = settlements, accountId = accountId),
                )
            }
        }

    override fun streamLedgerPage(
        accountId: String,
        limit: Int,
    ): Flow<AppResult<LedgerPageDomain>> =
        combine(store.expenses, store.settlements) { expenses, settlements ->
            AppResult.Success(
                LedgerPageDomain.merge(
                    expenses = accountExpenses(expenses = expenses, accountId = accountId),
                    settlements = accountSettlements(settlements = settlements, accountId = accountId),
                    limit = limit,
                ),
            )
        }

    override suspend fun getLedgerPage(
        accountId: String,
        after: LedgerCursorDomain,
        limit: Int,
    ): AppResult<LedgerPageDomain> = safeCall {
        LedgerPageDomain.merge(
            expenses =
                accountExpenses(expenses = store.expenses.value, accountId = accountId).filter {
                    LedgerRecordDomain.Purchase(expense = it).isAfter(cursor = after)
                },
            settlements =
                accountSettlements(settlements = store.settlements.value, accountId = accountId)
                    .filter { LedgerRecordDomain.Repayment(settlement = it).isAfter(cursor = after) },
            limit = limit,
        )
    }

    private fun accountExpenses(expenses: List<ExpenseDomain>, accountId: String): List<ExpenseDomain> {
        val account = accountId.ifBlank { store.context.id }
        return expenses
            .filter { it.accountId.ifBlank { store.context.id } == account }
            .map {
                it.copy(
                    accountId = account,
                    version = maxOf(1, it.version),
                    createdBy = it.createdBy.ifBlank { store.context.userId },
                )
            }
    }

    private fun accountSettlements(
        settlements: List<SettlementDomain>,
        accountId: String,
    ): List<SettlementDomain> {
        val account = accountId.ifBlank { store.context.id }
        return settlements
            .filter { it.accountId.ifBlank { store.context.id } == account }
            .map { it.copy(accountId = account) }
    }

    override suspend fun saveExpense(expense: ExpenseDomain) = safeCall {
        val previous = store.expenses.value.firstOrNull { it.id == expense.id }
        val exists = previous != null
        // Like the server, an edit keeps the original entry time and a new record gets now.
        val createdAt = previous?.createdAt ?: Clock.System.now().toString()
        store.expenses.update { expenses ->
            expenses.filterNot { it.id == expense.id } +
                expense.copy(version = expense.version + 1, createdAt = createdAt)
        }
        if (!exists) {
            if (expense.recurring) {
                val date = LocalDate.parse(expense.occurredOn)
                store.recurring.update {
                    it +
                        RecurringExpenseDomain(
                            newId(),
                            expense.title,
                            expense.amountMinor,
                            expense.paidBy,
                            expense.category,
                            MonthlyRecurrenceDomain.nextAfter(date, date.day).toString(),
                            date.day,
                            true,
                            false,
                            store.context.userId,
                            1,
                            expense.shares,
                            accountId = expense.accountId,
                            splitMethod = expense.splitMethod,
                            splitValues = expense.splitValues,
                        )
                }
            }
        }
        Unit
    }

    override suspend fun getExpense(id: String, accountId: String) = safeCall {
        store.expenses.value
            .first { it.id == id && it.accountId.ifBlank { store.context.id } == accountId }
            .let {
                it.copy(
                    accountId = accountId,
                    version = maxOf(1, it.version),
                    createdBy = it.createdBy.ifBlank { store.context.userId },
                )
            }
    }

    override suspend fun deleteExpense(expense: ExpenseDomain): AppResult<Unit> = safeCall {
        store.expenses.update { expenses -> expenses.filterNot { it.id == expense.id } }
    }

    override suspend fun getSettlement(id: String, accountId: String): AppResult<SettlementDomain> =
        safeCall {
            store.settlements.value
                .first {
                    it.id == id && it.accountId.ifBlank { store.context.id } == accountId
                }
                .copy(accountId = accountId)
        }

    override suspend fun saveSettlement(settlement: SettlementDomain) = safeCall {
        val before = store.settlements.value.first { it.id == settlement.id }
        requireDomain(before.version == settlement.version) { SettlementError.Changed() }
        val saved =
            settlement.copy(
                version = before.version + 1,
                createdBy = before.createdBy,
                createdAt = before.createdAt,
            )
        store.settlements.update { values -> values.map { if (it.id == saved.id) saved else it } }
        saved
    }

    override suspend fun removeSettlement(settlement: SettlementDomain) = safeCall {
        val before = store.settlements.value.firstOrNull { it.id == settlement.id }
        requireDomain(before == null || before.version == settlement.version) {
            SettlementError.Changed()
        }
        store.settlements.update { values -> values.filterNot { it.id == settlement.id } }
    }

    override suspend fun recordSettlement(settlement: SettlementDomain) = safeCall {
        store.settlements.update {
            it.filterNot { row -> row.id == settlement.id } +
                settlement.copy(
                    version = 1,
                    createdBy = store.context.userId,
                    createdAt = Clock.System.now().toString(),
                )
        }
        Unit
    }
}
