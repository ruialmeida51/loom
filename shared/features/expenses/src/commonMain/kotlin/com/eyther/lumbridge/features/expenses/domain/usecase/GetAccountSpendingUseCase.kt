package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseDataSource
import com.eyther.lumbridge.features.expenses.domain.model.AccountSpendingDomain

/** What was spent in an account and who paid it, for its shared summary. */
class GetAccountSpendingUseCase(
    private val expenseDataSource: ExpenseDataSource,
) {
    suspend operator fun invoke(accountId: String): AppResult<AccountSpendingDomain> =
        expenseDataSource.getSpending(accountId = accountId)
}
