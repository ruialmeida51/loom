package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.data.media.preparePicture
import com.eyther.lumbridge.core.domain.media.PictureLoader
import com.eyther.lumbridge.core.domain.newId

/** In-memory stand-in for the pictures bucket: like the server, only the current picture is kept. */
object DemoPictures : PictureLoader {
    private val stored = mutableMapOf<String, ByteArray>()

    override suspend fun load(path: String): ByteArray? = stored[path]

    /** Stores [bytes] under [folder], removing [previous]; returns the new path. */
    suspend fun replace(folder: String, bytes: ByteArray?, previous: String?): String? {
        previous?.let(stored::remove)
        if (bytes == null) return null
        val path = "$folder/${newId()}.jpg"
        stored[path] = preparePicture(bytes = bytes)
        return path
    }
}
