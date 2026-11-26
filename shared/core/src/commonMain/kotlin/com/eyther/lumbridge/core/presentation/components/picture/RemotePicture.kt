package com.eyther.lumbridge.core.presentation.components.picture

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.decodeToImageBitmap

private const val MAX_DECODED_PICTURES = 64

/**
 * Pictures already shown, decoded, so a screen that comes back (a tab switch, a toolbar on each
 * tab) draws them on its first frame instead of flashing the initials. Composition and
 * [produceState] both run on the main thread, so this needs no lock.
 */
private val decodedPictures = linkedMapOf<String, ImageBitmap>()

private fun recall(path: String?): ImageBitmap? =
    path?.let { key -> decodedPictures.remove(key)?.also { decodedPictures[key] = it } }

private fun remember(path: String, bitmap: ImageBitmap) {
    decodedPictures[path] = bitmap
    if (decodedPictures.size > MAX_DECODED_PICTURES) decodedPictures.remove(decodedPictures.keys.first())
}

/**
 * Shows the picture at [path], cropped to [shape], or [fallback] (initials) while it loads, when
 * there is none, or when it cannot be read.
 */
@Composable
fun RemotePicture(
    path: String?,
    shape: Shape,
    modifier: Modifier = Modifier,
    fallback: @Composable () -> Unit,
) {
    val loader = LocalPictureLoader.current
    val bitmap by
        produceState(initialValue = recall(path), key1 = path, key2 = loader) {
            // The state outlives a change of [path], so start from what that path has cached.
            value = recall(path)
            if (value != null || path == null) return@produceState
            value =
                loader.load(path)?.let { bytes ->
                    runCatching { bytes.decodeToImageBitmap() }.getOrNull()
                }
            value?.let { remember(path = path, bitmap = it) }
        }
    val image = bitmap
    if (image == null) Box(modifier) { fallback() }
    else
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(shape),
        )
}
