package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.MonthlyRecurrenceDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class DemoExpenseDataSource(private val store: DemoHouseholdStore) : ExpenseDataSource {

    override fun streamExpenses() = store.expenses.map { AppResult.Success(it) }

    override fun streamSettlements() = store.settlements.map { AppResult.Success(it) }

    override suspend fun addExpense(expense: ExpenseDomain) = safeCall {
        if (store.expenses.value.none { it.id == expense.id }) {
            store.expenses.update { it + expense }
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
                        )
                }
            }
        }
        Unit
    }

    override suspend fun recordSettlement(settlement: SettlementDomain) = safeCall {
        store.settlements.update { it.filterNot { row -> row.id == settlement.id } + settlement }
        Unit
    }
}
