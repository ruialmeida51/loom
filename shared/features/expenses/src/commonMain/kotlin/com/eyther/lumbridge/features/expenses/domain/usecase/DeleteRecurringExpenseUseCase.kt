package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain

class DeleteRecurringExpenseUseCase(
    private val recurringExpenseDataSource: RecurringExpenseDataSource,
) {
    suspend operator fun invoke(rule: RecurringExpenseDomain): AppResult<Unit> =
        recurringExpenseDataSource.delete(rule = rule)
}
