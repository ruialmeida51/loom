package com.eyther.lumbridge.features.expenses.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.data.media.prepareReceiptPhoto
import com.eyther.lumbridge.features.expenses.data.network.mapper.toDomain
import com.eyther.lumbridge.features.expenses.data.network.mapper.toReceiptError
import com.eyther.lumbridge.features.expenses.data.network.model.request.SaveExpenseReceiptRequest
import com.eyther.lumbridge.features.expenses.data.network.model.response.ExpenseReceiptResponse
import com.eyther.lumbridge.features.expenses.domain.datasource.ExpenseReceiptDataSource
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseReceiptDomain
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.bytestring.ByteString

class CommonExpenseReceiptDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ExpenseReceiptDataSource {
    private val photoMutex = Mutex()
    private val downloadedPhotos = linkedMapOf<String, ReceiptPhotoDomain>()

    override suspend fun preparePhoto(bytes: ByteArray): AppResult<ReceiptPhotoDomain> =
        prepareReceiptPhoto(bytes = bytes)

    override suspend fun getReceipt(
        expenseId: String,
        accountId: String,
    ): AppResult<ExpenseReceiptDomain> =
        safeApiCall(mapError = Exception::toReceiptError) {
            access.read { client ->
                client
                    .from("expense_receipts")
                    .select {
                        filter {
                            eq("household_id", context.id)
                            eq("account_id", accountId)
                            eq("expense_id", expenseId)
                        }
                    }
                    .decodeList<ExpenseReceiptResponse>()
                    .singleOrNull()
                    ?.toDomain()
                    ?: ExpenseReceiptDomain(expenseId = expenseId, accountId = accountId)
            }
        }

    override suspend fun downloadPhoto(
        receipt: ExpenseReceiptDomain,
    ): AppResult<ReceiptPhotoDomain> =
        safeApiCall(mapError = Exception::toReceiptError) {
            val path = receipt.objectPath ?: throw ReceiptError.NotFound()
            photoMutex.withLock {
                downloadedPhotos.remove(path)?.let { cached ->
                    downloadedPhotos[path] = cached
                    return@withLock cached
                }
                val bytes = access.read { client ->
                    client.storage[RECEIPT_BUCKET].downloadAuthenticated(path = path)
                }
                if (bytes.isEmpty() || bytes.size > ReceiptPhotoDomain.MAX_STORED_BYTES)
                    throw ReceiptError.InvalidPhoto()
                val photo = ReceiptPhotoDomain(data = ByteString(bytes), id = path)
                downloadedPhotos[path] = photo
                if (downloadedPhotos.size > MAX_CACHED_PHOTOS)
                    downloadedPhotos.remove(downloadedPhotos.keys.first())
                photo
            }
        }

    override suspend fun savePhoto(
        receipt: ExpenseReceiptDomain,
        photo: ReceiptPhotoDomain,
    ): AppResult<ExpenseReceiptDomain> =
        safeApiCall(mapError = Exception::toReceiptError) {
            if (photo.data.size !in 1..ReceiptPhotoDomain.MAX_UPLOAD_BYTES)
                throw ReceiptError.PhotoTooLarge()
            val path = "${context.id}/${receipt.accountId}/${receipt.expenseId}/${photo.id}.jpg"
            val client = access.requireClient()
            // A stable draft path reconciles a committed upload whose response was lost.
            val current =
                client
                    .from("expense_receipts")
                    .select {
                        filter {
                            eq("household_id", context.id)
                            eq("expense_id", receipt.expenseId)
                            eq("account_id", receipt.accountId)
                        }
                    }
                    .decodeList<ExpenseReceiptResponse>()
                    .singleOrNull()
                    ?.toDomain()
            if (current?.objectPath == path) return@safeApiCall current
            val bucket = client.storage[RECEIPT_BUCKET]
            if (!bucket.exists(path))
                bucket.upload(path = path, data = photo.data.toByteArray()) {
                    upsert = false
                    contentType = ContentType.Image.JPEG
                }
            access.write { writer ->
                writer.postgrest
                    .rpc(
                        "save_expense_receipt",
                        SaveExpenseReceiptRequest(
                            householdId = context.id,
                            accountId = receipt.accountId,
                            expenseId = receipt.expenseId,
                            objectPath = path,
                            version = receipt.version,
                        ),
                    ) {
                        single()
                    }
                    .decodeAs<ExpenseReceiptResponse>()
                    .toDomain()
            }
        }

    override suspend fun removePhoto(
        receipt: ExpenseReceiptDomain,
    ): AppResult<ExpenseReceiptDomain> =
        safeApiCall(mapError = Exception::toReceiptError) {
            access.write { client ->
                client
                    .postgrest
                    .rpc(
                        "save_expense_receipt",
                        SaveExpenseReceiptRequest(
                            householdId = context.id,
                            accountId = receipt.accountId,
                            expenseId = receipt.expenseId,
                            objectPath = null,
                            version = receipt.version,
                        ),
                    ) {
                        single()
                    }
                    .decodeAs<ExpenseReceiptResponse>()
                    .toDomain()
            }
        }
}

private const val RECEIPT_BUCKET = "expense-receipts"
private const val MAX_CACHED_PHOTOS = 2
