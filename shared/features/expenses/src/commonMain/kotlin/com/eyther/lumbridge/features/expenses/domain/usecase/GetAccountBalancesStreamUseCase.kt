package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import kotlinx.coroutines.flow.Flow

/** Everyone's balance in an account; a positive balance means the person is owed money. */
class GetAccountBalancesStreamUseCase(
    private val expenseDataSource: ExpenseDataSource,
) {

    operator fun invoke(accountId: String = ""): Flow<AppResult<Map<String, Long>>> =
        expenseDataSource.streamBalances(accountId = accountId)
}
