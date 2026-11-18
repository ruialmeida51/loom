package com.eyther.lumbridge.features.expenses.data.media

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.compressImage

// Keep receipt text readable while bounding decoding memory and upload/egress size.
private const val ReceiptMaximumEdge = 2560
private const val ReceiptJpegQuality = 85

internal actual suspend fun compressReceiptPhoto(bytes: ByteArray): ByteArray =
    FileKit.compressImage(
        bytes = bytes,
        quality = ReceiptJpegQuality,
        maxWidth = ReceiptMaximumEdge,
        maxHeight = ReceiptMaximumEdge,
    )
