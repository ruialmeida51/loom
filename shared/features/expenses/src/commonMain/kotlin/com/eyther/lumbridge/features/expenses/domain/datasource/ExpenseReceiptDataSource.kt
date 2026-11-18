package com.eyther.lumbridge.features.expenses.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain

interface ExpenseReceiptDataSource {
    suspend fun preparePhoto(bytes: ByteArray): AppResult<ReceiptPhotoDomain>

    suspend fun getReceipt(expenseId: String, accountId: String): AppResult<ExpenseReceiptDomain>

    suspend fun downloadPhoto(receipt: ExpenseReceiptDomain): AppResult<ReceiptPhotoDomain>

    suspend fun savePhoto(
        receipt: ExpenseReceiptDomain,
        photo: ReceiptPhotoDomain,
    ): AppResult<ExpenseReceiptDomain>

    suspend fun removePhoto(receipt: ExpenseReceiptDomain): AppResult<ExpenseReceiptDomain>
}
