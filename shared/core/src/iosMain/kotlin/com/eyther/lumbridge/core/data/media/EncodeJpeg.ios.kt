package com.eyther.lumbridge.core.data.media

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.compressImage

actual suspend fun encodeJpeg(
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
        if (encoded.isEmpty()) throw IllegalArgumentException(UNREADABLE_IMAGE)
        if (encoded.size <= maximumBytes) return encoded
    }
    return null
}
