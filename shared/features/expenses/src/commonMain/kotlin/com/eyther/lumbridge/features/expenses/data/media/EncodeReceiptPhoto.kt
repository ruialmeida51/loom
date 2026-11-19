package com.eyther.lumbridge.features.expenses.data.media

/** Native codecs normalize orientation and discard original metadata. */
internal expect suspend fun encodeReceiptPhoto(
    bytes: ByteArray,
    maximumEdge: Int,
    jpegQuality: Int,
): ByteArray
