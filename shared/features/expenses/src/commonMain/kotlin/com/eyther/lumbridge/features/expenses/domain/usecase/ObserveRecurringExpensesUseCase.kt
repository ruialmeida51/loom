package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.features.expenses.domain.datasource.RecurringExpenseDataSource

class ObserveRecurringExpensesUseCase(private val source: RecurringExpenseDataSource) {
    operator fun invoke() = source.observe()
}
