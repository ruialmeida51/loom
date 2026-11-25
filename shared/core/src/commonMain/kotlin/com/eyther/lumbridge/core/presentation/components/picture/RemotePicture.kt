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
        produceState<ImageBitmap?>(initialValue = null, key1 = path, key2 = loader) {
            value =
                path?.let { loader.load(it) }?.let { bytes ->
                    runCatching { bytes.decodeToImageBitmap() }.getOrNull()
                }
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
