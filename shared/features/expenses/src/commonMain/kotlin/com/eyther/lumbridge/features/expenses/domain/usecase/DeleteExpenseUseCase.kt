package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain

class DeleteExpenseUseCase(private val expenseDataSource: ExpenseDataSource) {
    suspend operator fun invoke(expense: ExpenseDomain): AppResult<Unit> =
        expenseDataSource.deleteExpense(expense = expense)
}
