package com.eyther.lumbridge.features.expenses.data.media

import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import kotlinx.io.bytestring.ByteString

suspend fun prepareReceiptPhoto(bytes: ByteArray): AppResult<ReceiptPhotoDomain> =
    safeApiCall(
        mapError = { if (it is AppError) it else ReceiptError.InvalidPhoto(cause = it) },
    ) {
        if (bytes.isEmpty()) throw ReceiptError.InvalidPhoto()
        if (bytes.size > ReceiptPhotoDomain.MAX_SOURCE_BYTES) throw ReceiptError.PhotoTooLarge()
        val compressed = compressReceiptPhoto(bytes = bytes)
        if (compressed.isEmpty()) throw ReceiptError.InvalidPhoto()
        if (compressed.size > ReceiptPhotoDomain.MAX_UPLOAD_BYTES)
            throw ReceiptError.PhotoTooLarge()
        ReceiptPhotoDomain(data = ByteString(compressed))
    }
