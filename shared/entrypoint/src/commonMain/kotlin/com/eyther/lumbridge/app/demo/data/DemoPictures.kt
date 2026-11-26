package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.data.media.preparePicture
import com.eyther.lumbridge.core.domain.media.PictureLoader
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.shared.entrypoint.generated.resources.Res

/** Demo stories' pictures stand in for publishers' web addresses and ship with the app. */
const val DEMO_NEWS_PICTURES = "https://pictures.loom.example/"

/** In-memory stand-in for the pictures bucket: like the server, only the current picture is kept. */
object DemoPictures : PictureLoader {
    private val stored = mutableMapOf<String, ByteArray>()

    override suspend fun load(path: String): ByteArray? =
        stored[path]
            ?: path.removePrefix(DEMO_NEWS_PICTURES).takeIf { it != path }?.let { name ->
                runCatching { Res.readBytes("files/demo/$name") }.getOrNull()
            }

    /** Stores [bytes] under [folder], removing [previous]; returns the new path. */
    suspend fun replace(folder: String, bytes: ByteArray?, previous: String?): String? {
        previous?.let(stored::remove)
        if (bytes == null) return null
        val path = "$folder/${newId()}.jpg"
        stored[path] = preparePicture(bytes = bytes)
        return path
    }
}
