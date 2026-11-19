package com.eyther.lumbridge.features.expenses.data.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

/** Sample before allocating pixels; full-resolution camera images can exhaust the app heap. */
internal actual suspend fun encodeReceiptPhoto(
    bytes: ByteArray,
    maximumEdge: Int,
    jpegQuality: Int,
): ByteArray {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw ReceiptError.InvalidPhoto()
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maximumEdge) sample *= 2
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    val decoded =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: throw ReceiptError.InvalidPhoto()
    var oriented: Bitmap? = null
    try {
        val orientation =
            try {
                try {
                    ByteArrayInputStream(bytes).use {
                        ExifInterface(it)
                            .getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL,
                            )
                    }
                } catch (_: IOException) {
                    ExifInterface.ORIENTATION_NORMAL
                }
            } catch (_: IOException) {
                ExifInterface.ORIENTATION_NORMAL
            }
        val matrix =
            Matrix().apply {
                when (orientation) {
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> setScale(-1f, 1f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> setRotate(180f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> setScale(1f, -1f)
                    ExifInterface.ORIENTATION_TRANSPOSE -> {
                        setRotate(90f)
                        postScale(-1f, 1f)
                    }
                    ExifInterface.ORIENTATION_ROTATE_90 -> setRotate(90f)
                    ExifInterface.ORIENTATION_TRANSVERSE -> {
                        setRotate(270f)
                        postScale(-1f, 1f)
                    }
                    ExifInterface.ORIENTATION_ROTATE_270 -> setRotate(270f)
                }
            }
        oriented = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
        return ByteArrayOutputStream().use { output ->
            if (!oriented.compress(Bitmap.CompressFormat.JPEG, jpegQuality, output))
                throw ReceiptError.InvalidPhoto()
            output.toByteArray()
        }
    } finally {
        if (oriented !== decoded) oriented?.recycle()
        decoded.recycle()
    }
}
