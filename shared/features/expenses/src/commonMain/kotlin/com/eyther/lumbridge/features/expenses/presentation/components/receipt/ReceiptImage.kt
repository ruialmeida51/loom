package com.eyther.lumbridge.features.expenses.presentation.components.receipt

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
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
) {
    val bitmap by
        produceState<ImageBitmap?>(initialValue = null, key1 = photo.id) {
            val decoded =
                withContext(Dispatchers.Default) {
                    runCatching { photo.data.toByteArray().decodeToImageBitmap() }
                }
            value = decoded.getOrNull()
            if (decoded.isFailure) onInvalidPhoto()
        }
    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = stringResource(Res.string.receipt_photo_description),
            contentScale = ContentScale.Fit,
            modifier = modifier,
        )
    }
}
