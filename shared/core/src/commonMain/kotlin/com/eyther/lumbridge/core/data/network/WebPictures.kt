package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.data.media.encodeJpeg
import com.eyther.lumbridge.core.domain.media.PictureLoader
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.readByteArray

/** Story pictures fill a card about 264–360dp wide, so 800px stays sharp on a 3x screen. */
private const val WEB_PICTURE_MAXIMUM_EDGE = 800
private val WebPictureJpegQualities = listOf(80, 70)
private const val WEB_PICTURE_MAXIMUM_BYTES = 200_000
private const val WEB_PICTURE_SOURCE_BYTES = 5_000_000L
private const val MAX_CACHED_WEB_PICTURES = 40

/** Whether [path] names a public picture on the web rather than one in the pictures bucket. */
fun isWebPicture(path: String): Boolean = path.startsWith("https://")

/**
 * Downloads publishers' story pictures over https, shrinks each to a small JPEG and keeps the most
 * recent in memory, so scrolling back through the news does not fetch them again.
 */
class WebPictures(private val client: HttpClient) : PictureLoader {
    private val mutex = Mutex()
    private val cache = linkedMapOf<String, ByteArray>()

    override suspend fun load(path: String): ByteArray? {
        if (!isWebPicture(path)) return null
        mutex.withLock {
            cache.remove(path)?.let { cached ->
                cache[path] = cached
                return cached
            }
        }
        val picture =
            try {
                download(path = path)?.let { bytes ->
                    encodeJpeg(
                        bytes = bytes,
                        maximumEdge = WEB_PICTURE_MAXIMUM_EDGE,
                        jpegQualities = WebPictureJpegQualities,
                        maximumBytes = WEB_PICTURE_MAXIMUM_BYTES,
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            }
        if (picture == null || picture.isEmpty()) return null
        mutex.withLock {
            cache[path] = picture
            if (cache.size > MAX_CACHED_WEB_PICTURES) cache.remove(cache.keys.first())
        }
        return picture
    }

    private suspend fun download(path: String): ByteArray? {
        val response = client.get(path)
        if (!response.status.isSuccess()) return null
        if ((response.contentLength() ?: 0) > WEB_PICTURE_SOURCE_BYTES) return null
        val bytes = response.bodyAsChannel().readRemaining(WEB_PICTURE_SOURCE_BYTES + 1).readByteArray()
        return bytes.takeIf { it.isNotEmpty() && it.size <= WEB_PICTURE_SOURCE_BYTES }
    }
}
