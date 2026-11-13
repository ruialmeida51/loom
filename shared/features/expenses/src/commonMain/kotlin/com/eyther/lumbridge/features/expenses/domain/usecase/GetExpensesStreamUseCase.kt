package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import kotlinx.coroutines.flow.Flow

class GetExpensesStreamUseCase(
    private val expenseDataSource: ExpenseDataSource,
) {

    operator fun invoke(): Flow<AppResult<List<ExpenseDomain>>> = expenseDataSource.streamExpenses()
}
