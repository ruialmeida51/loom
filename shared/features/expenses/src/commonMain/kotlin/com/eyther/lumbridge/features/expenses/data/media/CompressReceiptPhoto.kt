package com.eyther.lumbridge.features.expenses.data.media

import com.eyther.lumbridge.core.data.media.encodeJpeg
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain

private val ReceiptMaximumEdges = listOf(2560, 2048, 1600)
private val ReceiptJpegQualities = listOf(85, 75, 65)

/** Prefer readable resolution before reducing dimensions to meet the upload budget. */
internal suspend fun compressReceiptPhoto(bytes: ByteArray): ByteArray {
    for (maximumEdge in ReceiptMaximumEdges) {
        encodeJpeg(
                bytes = bytes,
                maximumEdge = maximumEdge,
                jpegQualities = ReceiptJpegQualities,
                maximumBytes = ReceiptPhotoDomain.MAX_UPLOAD_BYTES,
            )
            ?.let { return it }
    }
    throw ReceiptError.PhotoTooLarge()
}
