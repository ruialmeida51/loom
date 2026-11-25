package com.eyther.lumbridge.core.data.media

import com.eyther.lumbridge.core.domain.errors.PictureError
import kotlinx.coroutines.CancellationException

/** Avatars render at most ~96dp; 640px keeps them sharp on dense screens at a small size. */
private const val PICTURE_MAXIMUM_EDGE = 640
private val PictureJpegQualities = listOf(85, 75, 65)

/** Stored pictures stay well under the bucket's 1 MB limit. */
const val PICTURE_MAXIMUM_BYTES = 400_000

/** Source files larger than this are refused before decoding. */
const val PICTURE_MAXIMUM_SOURCE_BYTES = 25_000_000

/** Resizes and re-encodes a picked image as a small JPEG without its original metadata. */
suspend fun preparePicture(bytes: ByteArray): ByteArray {
    if (bytes.isEmpty()) throw PictureError.Unreadable()
    if (bytes.size > PICTURE_MAXIMUM_SOURCE_BYTES) throw PictureError.TooLarge()
    val encoded =
        try {
            encodeJpeg(
                bytes = bytes,
                maximumEdge = PICTURE_MAXIMUM_EDGE,
                jpegQualities = PictureJpegQualities,
                maximumBytes = PICTURE_MAXIMUM_BYTES,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            throw PictureError.Unreadable(cause = error)
        }
    return encoded?.takeIf { it.isNotEmpty() } ?: throw PictureError.TooLarge()
}
