package com.eyther.lumbridge.features.expenses.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain

class PrepareReceiptPhotoUseCase(private val expenseReceiptDataSource: ExpenseReceiptDataSource) {
    suspend operator fun invoke(bytes: ByteArray): AppResult<ReceiptPhotoDomain> =
        expenseReceiptDataSource.preparePhoto(bytes = bytes)
}
