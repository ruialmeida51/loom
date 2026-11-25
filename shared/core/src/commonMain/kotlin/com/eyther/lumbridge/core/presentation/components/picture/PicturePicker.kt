package com.eyther.lumbridge.core.presentation.components.picture

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import com.eyther.lumbridge.core.data.media.PICTURE_MAXIMUM_SOURCE_BYTES
import com.eyther.lumbridge.core.domain.errors.PictureError
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

/**
 * Remember at the Screen root (iOS presents from a stable view controller). Returns the action that
 * opens the photo library; the picked image's bytes go to [onPicked].
 */
@Composable
fun rememberPicturePicker(
    onPicked: (ByteArray) -> Unit,
    onError: (PictureError) -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    val picked by rememberUpdatedState(onPicked)
    val failed by rememberUpdatedState(onError)
    val launcher =
        rememberFilePickerLauncher(
            type = FileKitType.Image,
            onError = { failed(PictureError.Unreadable(cause = it)) },
        ) { file ->
            if (file == null) return@rememberFilePickerLauncher
            scope.launch {
                try {
                    if (file.size() > PICTURE_MAXIMUM_SOURCE_BYTES) throw PictureError.TooLarge()
                    picked(file.readBytes())
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (error: PictureError) {
                    failed(error)
                } catch (error: Exception) {
                    failed(PictureError.Unreadable(cause = error))
                }
            }
        }
    return { launcher.launch() }
}
