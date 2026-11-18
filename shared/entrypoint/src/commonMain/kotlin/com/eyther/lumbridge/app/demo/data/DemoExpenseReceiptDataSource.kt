package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.media.prepareReceiptPhoto
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import kotlinx.io.bytestring.ByteString

class DemoExpenseReceiptDataSource(private val store: DemoHouseholdStore) :
    ExpenseReceiptDataSource {
    private val receipts = store.expenseReceipts
    private val photos = store.receiptPhotos

    override suspend fun preparePhoto(bytes: ByteArray): AppResult<ReceiptPhotoDomain> =
        prepareReceiptPhoto(bytes = bytes)

    override suspend fun getReceipt(
        expenseId: String,
        accountId: String,
    ): AppResult<ExpenseReceiptDomain> =
        AppResult.Success(
            receipts[expenseId]?.takeIf { it.accountId == accountId }
                ?: ExpenseReceiptDomain(expenseId = expenseId, accountId = accountId),
        )

    override suspend fun downloadPhoto(
        receipt: ExpenseReceiptDomain,
    ): AppResult<ReceiptPhotoDomain> {
        val path = receipt.objectPath ?: return AppResult.Failure(ReceiptError.NotFound())
        val photo =
            photos[receipt.expenseId]
                ?: if (path.startsWith("demo/"))
                    ReceiptPhotoDomain(data = ByteString(loadDemoReceiptBytes()), id = path)
                else null
        return photo?.let { AppResult.Success(it) }
            ?: AppResult.Failure(ReceiptError.PhotoUnavailable())
    }

    override suspend fun savePhoto(
        receipt: ExpenseReceiptDomain,
        photo: ReceiptPhotoDomain,
    ): AppResult<ExpenseReceiptDomain> {
        if ((receipts[receipt.expenseId]?.version ?: 0) != receipt.version)
            return AppResult.Failure(ReceiptError.ReceiptChanged())
        val saved = receipt.copy(objectPath = photo.id, version = receipt.version + 1)
        receipts[receipt.expenseId] = saved
        photos[receipt.expenseId] = photo
        return AppResult.Success(saved)
    }

    override suspend fun removePhoto(
        receipt: ExpenseReceiptDomain,
    ): AppResult<ExpenseReceiptDomain> {
        if (receipts[receipt.expenseId]?.version != receipt.version)
            return AppResult.Failure(ReceiptError.ReceiptChanged())
        val saved = receipt.copy(objectPath = null, version = receipt.version + 1)
        receipts[receipt.expenseId] = saved
        photos.remove(receipt.expenseId)
        return AppResult.Success(saved)
    }
}
