package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain

class GetExpenseAccountUseCase(private val expenseAccountDataSource: ExpenseAccountDataSource) {
    suspend operator fun invoke(id: String): AppResult<ExpenseAccountDomain> =
        expenseAccountDataSource.getAccount(id = id)
}
