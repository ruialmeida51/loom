package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.core.domain.newId
import kotlinx.io.bytestring.ByteString

/** A bounded, normalized JPEG; immutable bytes keep an in-progress receipt safe across retries. */
data class ReceiptPhotoDomain(val data: ByteString, val id: String = newId()) {
    companion object {
        const val MAX_SOURCE_BYTES: Int = 25 * 1024 * 1024
        const val MAX_UPLOAD_BYTES: Int = 5 * 1024 * 1024
    }
}
