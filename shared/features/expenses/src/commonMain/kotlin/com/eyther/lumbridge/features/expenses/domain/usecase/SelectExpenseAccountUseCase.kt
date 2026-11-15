package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource

class SelectExpenseAccountUseCase(private val expenseAccountDataSource: ExpenseAccountDataSource) {
    suspend operator fun invoke(id: String): AppResult<Unit> = safeCall {
        expenseAccountDataSource.getAccount(id = id).getOrThrow()
        expenseAccountDataSource.selectAccount(id = id).getOrThrow()
    }
}
