package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import kotlinx.coroutines.flow.Flow

class GetExpenseAccountsStreamUseCase(
    private val expenseAccountDataSource: ExpenseAccountDataSource,
) {
    operator fun invoke(): Flow<AppResult<List<ExpenseAccountDomain>>> =
        expenseAccountDataSource.stream()
}
