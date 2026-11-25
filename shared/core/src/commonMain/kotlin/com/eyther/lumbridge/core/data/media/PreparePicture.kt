package com.eyther.lumbridge.core.data.media

import com.eyther.lumbridge.core.domain.errors.PictureError
import kotlinx.coroutines.CancellationException

/**
 * Pictures only ever render as avatars and badges (88dp at most, ~264px on a 3x screen), so 320px
 * keeps them sharp while each download stays a few tens of kilobytes. Supabase's free tier serves
 * files as stored, so this on-device resize is the only compression step.
 */
private const val PICTURE_MAXIMUM_EDGE = 320
private val PictureJpegQualities = listOf(80, 70, 60)

/** A stored picture's upper bound; typical results are 20–60 KB. */
const val PICTURE_MAXIMUM_BYTES = 120_000

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
