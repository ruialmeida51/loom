package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource

class ObserveExpensesUseCase(private val source: ExpenseDataSource) {
    operator fun invoke() = source.observeExpenses()
}
