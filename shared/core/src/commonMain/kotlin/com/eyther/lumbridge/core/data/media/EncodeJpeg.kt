package com.eyther.lumbridge.core.data.media

/**
 * Native codecs normalize orientation and discard original metadata. Returns the first encoding
 * at [maximumEdge] whose size fits [maximumBytes], trying [jpegQualities] in order, or null.
 * Implementations decode the source once per call where the platform codec allows it. An
 * undecodable source throws [IllegalArgumentException], which callers map to their own error.
 */
expect suspend fun encodeJpeg(
    bytes: ByteArray,
    maximumEdge: Int,
    jpegQualities: List<Int>,
    maximumBytes: Int,
): ByteArray?

internal const val UNREADABLE_IMAGE = "The image could not be decoded."
