package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain

class GetExpenseUseCase(private val expenseDataSource: ExpenseDataSource) {
    suspend operator fun invoke(id: String, accountId: String): AppResult<ExpenseDomain> =
        expenseDataSource.getExpense(id = id, accountId = accountId)
}
