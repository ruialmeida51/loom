package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.data.media.PICTURE_MAXIMUM_BYTES
import com.eyther.lumbridge.core.data.media.preparePicture
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.media.PictureLoader
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val PICTURE_BUCKET = "pictures"
private const val MAX_CACHED_PICTURES = 48

/**
 * Uploads and downloads profile/household pictures. Each upload gets a fresh path; the owning
 * feature then links it through its server function, which queues the previous picture for
 * deletion so only one is ever stored per profile or household.
 */
class PictureStorage(private val access: SupabaseAccess) : PictureLoader {
    private val mutex = Mutex()
    private val cache = linkedMapOf<String, ByteArray>()

    /** Prepares [bytes] and stores them under [folder] (such as `profiles/<id>`); returns the path. */
    suspend fun upload(folder: String, bytes: ByteArray): String {
        val picture = preparePicture(bytes = bytes)
        val path = "$folder/${newId()}.jpg"
        access.read { client ->
            client.storage[PICTURE_BUCKET].upload(path = path, data = picture) {
                upsert = false
                contentType = ContentType.Image.JPEG
            }
        }
        remember(path = path, bytes = picture)
        return path
    }

    /** Withdraws an upload that could not be linked, so it does not wait for the hourly sweep. */
    suspend fun discard(path: String) {
        runCatching { access.read { client -> client.storage[PICTURE_BUCKET].delete(path) } }
    }

    override suspend fun load(path: String): ByteArray? {
        mutex.withLock {
            cache.remove(path)?.let { cached ->
                cache[path] = cached
                return cached
            }
        }
        val bytes =
            runCatching {
                    access.read { client ->
                        client.storage[PICTURE_BUCKET].downloadAuthenticated(path = path)
                    }
                }
                .getOrNull()
                ?.takeIf { it.isNotEmpty() && it.size <= PICTURE_MAXIMUM_BYTES * 2 }
                ?: return null
        remember(path = path, bytes = bytes)
        return bytes
    }

    private suspend fun remember(path: String, bytes: ByteArray) {
        mutex.withLock {
            cache[path] = bytes
            if (cache.size > MAX_CACHED_PICTURES) cache.remove(cache.keys.first())
        }
    }
}
