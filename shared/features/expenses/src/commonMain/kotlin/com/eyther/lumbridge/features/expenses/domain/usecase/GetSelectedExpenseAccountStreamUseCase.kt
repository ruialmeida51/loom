package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseAccountDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ExpenseAccountError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetSelectedExpenseAccountStreamUseCase(
    private val expenseAccountDataSource: ExpenseAccountDataSource,
) {
    operator fun invoke(): Flow<AppResult<ExpenseAccountDomain>> =
        combine(
            expenseAccountDataSource.stream(),
            expenseAccountDataSource.streamSelection(),
        ) { result, selectedId ->
            safeCall {
                val accounts = result.getOrThrow()
                accounts.firstOrNull { it.id == selectedId }
                    ?: accounts.firstOrNull { it.isDefault }
                    ?: throw ExpenseAccountError.NotFound()
            }
        }
}
