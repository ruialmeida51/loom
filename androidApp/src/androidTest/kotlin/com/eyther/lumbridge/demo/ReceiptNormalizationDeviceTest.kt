package com.eyther.lumbridge.demo

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.expenses.data.media.prepareReceiptPhoto
import java.io.ByteArrayOutputStream
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptNormalizationDeviceTest {
    @Test
    fun largeCameraImagesAreBoundedOrientedAndStrippedOfExifOnRepeatedReturns() = runBlocking {
        val image = Bitmap.createBitmap(4096, 3072, Bitmap.Config.ARGB_8888)
        image.eraseColor(Color.WHITE)
        val encoded =
            ByteArrayOutputStream().use { output ->
                image.compress(Bitmap.CompressFormat.JPEG, 92, output)
                output.toByteArray()
            }
        image.recycle()
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val photo = File.createTempFile("loom-receipt", ".jpg", context.cacheDir)
        try {
            photo.writeBytes(encoded)
            ExifInterface(photo.path).apply {
                setAttribute(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_ROTATE_90.toString(),
                )
                setAttribute(ExifInterface.TAG_USER_COMMENT, "Must not leave the device")
                saveAttributes()
            }
            repeat(3) {
                val normalized =
                    prepareReceiptPhoto(photo.readBytes()).getOrThrow().data.toByteArray()
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(normalized, 0, normalized.size, bounds)
                assertTrue(maxOf(bounds.outWidth, bounds.outHeight) <= 2560)
                assertTrue(bounds.outHeight > bounds.outWidth)
                assertEquals("image/jpeg", bounds.outMimeType)
                val metadata = ExifInterface(normalized.inputStream())
                assertEquals(null, metadata.getAttribute(ExifInterface.TAG_USER_COMMENT))
            }
        } finally {
            photo.delete()
        }
    }
}
