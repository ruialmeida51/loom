package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain

class DeleteExpenseAccountUseCase(private val expenseAccountDataSource: ExpenseAccountDataSource) {
    suspend operator fun invoke(account: ExpenseAccountDomain): AppResult<Unit> =
        expenseAccountDataSource.deleteAccount(account = account)
}
