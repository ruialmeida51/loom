package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain

class GetExpenseReceiptUseCase(private val expenseReceiptDataSource: ExpenseReceiptDataSource) {
    suspend operator fun invoke(
        expenseId: String,
        accountId: String,
    ): AppResult<ExpenseReceiptDomain> =
        expenseReceiptDataSource.getReceipt(expenseId = expenseId, accountId = accountId)
}
