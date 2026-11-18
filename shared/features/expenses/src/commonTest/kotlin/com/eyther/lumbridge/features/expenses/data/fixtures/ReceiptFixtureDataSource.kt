package com.eyther.lumbridge.features.expenses.data.fixtures

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.io.bytestring.ByteString

class ReceiptFixtureDataSource : ExpenseReceiptDataSource {
    var saveCalls = 0
    var prepareError: AppError? = null
    var saveError: AppError? = null
    var saveGate: CompletableDeferred<AppResult<ExpenseReceiptDomain>>? = null
    var receipt: ExpenseReceiptDomain? = null
    var photo: ReceiptPhotoDomain? = null

    override suspend fun preparePhoto(bytes: ByteArray): AppResult<ReceiptPhotoDomain> =
        prepareError?.let { AppResult.Failure(it) }
            ?: AppResult.Success(ReceiptPhotoDomain(data = ByteString(bytes)))

    override suspend fun getReceipt(
        expenseId: String,
        accountId: String,
    ): AppResult<ExpenseReceiptDomain> =
        AppResult.Success(
            receipt ?: ExpenseReceiptDomain(expenseId = expenseId, accountId = accountId),
        )

    override suspend fun downloadPhoto(
        receipt: ExpenseReceiptDomain,
    ): AppResult<ReceiptPhotoDomain> =
        photo?.let { AppResult.Success(it) } ?: AppResult.Failure(ReceiptError.PhotoUnavailable())

    override suspend fun savePhoto(
        receipt: ExpenseReceiptDomain,
        photo: ReceiptPhotoDomain,
    ): AppResult<ExpenseReceiptDomain> {
        saveCalls++
        saveGate?.let {
            return it.await()
        }
        saveError?.let {
            return AppResult.Failure(it)
        }
        if ((this.receipt?.version ?: 0) != receipt.version)
            return AppResult.Failure(ReceiptError.ReceiptChanged())
        val saved = receipt.copy(objectPath = photo.id, version = receipt.version + 1)
        this.receipt = saved
        this.photo = photo
        return AppResult.Success(saved)
    }

    override suspend fun removePhoto(
        receipt: ExpenseReceiptDomain,
    ): AppResult<ExpenseReceiptDomain> {
        if (this.receipt?.version != receipt.version)
            return AppResult.Failure(ReceiptError.ReceiptChanged())
        val saved = receipt.copy(objectPath = null, version = receipt.version + 1)
        this.receipt = saved
        photo = null
        return AppResult.Success(saved)
    }
}
