package com.eyther.lumbridge.core.domain.media

/**
 * Loads a stored picture's bytes by path. Like an image loader, it is provided once at the app
 * root so avatars anywhere can render without each screen fetching pictures itself.
 */
fun interface PictureLoader {
    suspend fun load(path: String): ByteArray?
}
