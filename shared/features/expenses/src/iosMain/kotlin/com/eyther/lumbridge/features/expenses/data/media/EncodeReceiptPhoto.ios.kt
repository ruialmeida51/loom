package com.eyther.lumbridge.features.expenses.data.media

import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.compressImage

internal actual suspend fun encodeReceiptPhoto(
    bytes: ByteArray,
    maximumEdge: Int,
    jpegQualities: List<Int>,
    maximumBytes: Int,
): ByteArray? {
    for (jpegQuality in jpegQualities) {
        val encoded =
            FileKit.compressImage(
                bytes = bytes,
                quality = jpegQuality,
                maxWidth = maximumEdge,
                maxHeight = maximumEdge,
            )
        if (encoded.isEmpty()) throw ReceiptError.InvalidPhoto()
        if (encoded.size <= maximumBytes) return encoded
    }
    return null
}
