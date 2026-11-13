package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.*
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

class DemoExpenseDataSource(private val store: DemoHouseholdStore) : ExpenseDataSource {
    override fun observeExpenses() = store.expenses

    override fun observeSettlements() = store.settlements

    override suspend fun add(expense: ExpenseDomain) = safeCall {
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

    override suspend fun settle(settlement: SettlementDomain) = safeCall {
        store.settlements.update { it.filterNot { row -> row.id == settlement.id } + settlement }
        Unit
    }
}
