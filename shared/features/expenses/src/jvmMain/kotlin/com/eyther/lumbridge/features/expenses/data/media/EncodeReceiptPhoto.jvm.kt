package com.eyther.lumbridge.features.expenses.data.media

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.compressImage

internal actual suspend fun encodeReceiptPhoto(
    bytes: ByteArray,
    maximumEdge: Int,
    jpegQuality: Int,
): ByteArray =
    FileKit.compressImage(
        bytes = bytes,
        quality = jpegQuality,
        maxWidth = maximumEdge,
        maxHeight = maximumEdge,
    )
