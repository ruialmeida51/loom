package com.eyther.lumbridge.features.expenses.data.media

/**
 * Native codecs normalize orientation and discard original metadata. Returns the first encoding
 * at [maximumEdge] whose size fits [maximumBytes], trying [jpegQualities] in order, or null.
 * Implementations decode the source once per call where the platform codec allows it.
 */
internal expect suspend fun encodeReceiptPhoto(
    bytes: ByteArray,
    maximumEdge: Int,
    jpegQualities: List<Int>,
    maximumBytes: Int,
): ByteArray?
