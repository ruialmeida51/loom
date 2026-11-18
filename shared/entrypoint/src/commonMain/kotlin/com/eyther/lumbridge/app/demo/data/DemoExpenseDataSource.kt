package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.errors.requireDomain
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.SettlementError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.MonthlyRecurrenceDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class DemoExpenseDataSource(private val store: DemoHouseholdStore) : ExpenseDataSource {

    override fun streamExpenses(accountId: String) =
        store.expenses.map { expenses ->
            AppResult.Success(
                expenses
                    .filter { it.accountId.ifBlank { store.context.id } == accountId }
                    .map {
                        it.copy(
                            accountId = accountId,
                            version = maxOf(1, it.version),
                            createdBy = it.createdBy.ifBlank { store.context.userId },
                        )
                    },
            )
        }

    override fun streamSettlements(accountId: String) =
        store.settlements.map { settlements ->
            AppResult.Success(
                settlements
                    .filter { it.accountId.ifBlank { store.context.id } == accountId }
                    .map { it.copy(accountId = accountId) },
            )
        }

    override suspend fun saveExpense(expense: ExpenseDomain) = safeCall {
        val exists = store.expenses.value.any { it.id == expense.id }
        store.expenses.update { expenses ->
            expenses.filterNot { it.id == expense.id } + expense.copy(version = expense.version + 1)
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

    override suspend fun saveSettlement(settlement: SettlementDomain) = safeCall {
        val before = store.settlements.value.first { it.id == settlement.id }
        requireDomain(before.version == settlement.version) { SettlementError.Changed() }
        val saved = settlement.copy(version = before.version + 1, createdBy = before.createdBy)
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
                settlement.copy(version = 1, createdBy = store.context.userId)
        }
        Unit
    }
}
