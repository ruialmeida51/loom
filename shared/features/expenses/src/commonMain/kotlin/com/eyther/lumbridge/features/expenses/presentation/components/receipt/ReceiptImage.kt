package com.eyther.lumbridge.features.expenses.presentation.components.receipt

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.eyther.lumbridge.core.presentation.components.icon.Glyph
import com.eyther.lumbridge.core.presentation.components.icon.LoomIcon
import com.eyther.lumbridge.core.presentation.extensions.shimmer
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_photo_description
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun ReceiptImage(
    photo: ReceiptPhotoUi,
    modifier: Modifier = Modifier,
    onInvalidPhoto: () -> Unit,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val result by
        produceState<Result<ImageBitmap>?>(initialValue = null, key1 = photo.id) {
            val decoded =
                withContext(Dispatchers.Default) {
                    runCatching { photo.data.toByteArray().decodeToImageBitmap() }
                }
            value = decoded
            if (decoded.isFailure) onInvalidPhoto()
        }
    val bitmap = result?.getOrNull()
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = stringResource(Res.string.receipt_photo_description),
            contentScale = contentScale,
            modifier = modifier,
        )
    } else if (result == null)
        Box(modifier = modifier.shimmer(shouldShimmer = true, shape = MaterialTheme.shapes.medium))
    else
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            LoomIcon(glyph = Glyph.PHOTO, decorative = true)
        }
}
