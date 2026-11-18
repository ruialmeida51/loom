package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain

class DownloadReceiptPhotoUseCase(private val expenseReceiptDataSource: ExpenseReceiptDataSource) {
    suspend operator fun invoke(receipt: ExpenseReceiptDomain): AppResult<ReceiptPhotoDomain> =
        expenseReceiptDataSource.downloadPhoto(receipt = receipt)
}
