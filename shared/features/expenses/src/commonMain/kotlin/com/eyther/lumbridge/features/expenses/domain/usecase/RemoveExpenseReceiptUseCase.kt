package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain

class RemoveExpenseReceiptUseCase(private val expenseReceiptDataSource: ExpenseReceiptDataSource) {
    suspend operator fun invoke(receipt: ExpenseReceiptDomain): AppResult<ExpenseReceiptDomain> =
        expenseReceiptDataSource.removePhoto(receipt = receipt)
}
