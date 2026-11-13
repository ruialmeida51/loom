package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import kotlinx.coroutines.flow.Flow

class GetRecurringExpensesStreamUseCase(
    private val recurringExpenseDataSource: RecurringExpenseDataSource,
) {

    operator fun invoke(): Flow<AppResult<List<RecurringExpenseDomain>>> = recurringExpenseDataSource.stream()
}
