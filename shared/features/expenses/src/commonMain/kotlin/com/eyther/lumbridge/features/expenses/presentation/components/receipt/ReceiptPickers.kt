package com.eyther.lumbridge.features.expenses.presentation.components.receipt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import com.eyther.lumbridge.features.expenses.domain.model.ReceiptPhotoDomain
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Stable
internal class ReceiptPickers(
    val camera: ReceiptCameraLauncher,
    val gallery: () -> Unit,
    private val readingState: State<Boolean>,
) {
    val reading: Boolean
        get() = readingState.value
}

/** Remember at the Screen root so iOS always presents from a stable view controller. */
@Composable
internal fun rememberReceiptPickers(
    onPicked: (ByteArray) -> Unit,
    onError: (ReceiptError) -> Unit,
): ReceiptPickers {
    val scope = rememberCoroutineScope()
    val reading = remember { mutableStateOf(false) }
    fun accept(file: PlatformFile?, captured: Boolean) {
        if (file == null) return
        reading.value = true
        scope.launch {
            try {
                if (file.size() > ReceiptPhotoDomain.MAX_SOURCE_BYTES)
                    throw ReceiptError.PhotoTooLarge()
                onPicked(file.readBytes())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                onError(
                    if (error is ReceiptError) error
                    else ReceiptError.PhotoUnavailable(cause = error),
                )
            } finally {
                reading.value = false
                // Only our captured cache file is temporary; gallery originals belong to the user.
                if (captured)
                    withContext(NonCancellable) {
                        // Cleanup must not replace the picker outcome; the OS also reclaims this
                        // cache.
                        runCatching { file.delete() }
                    }
            }
        }
    }
    val gallery =
        rememberFilePickerLauncher(
            type = FileKitType.Image,
            onError = { onError(ReceiptError.PhotoUnavailable(cause = it)) },
        ) { file ->
            accept(file = file, captured = false)
        }
    val camera =
        rememberReceiptCameraLauncher(
            onResult = { accept(file = it, captured = true) },
            onError = { onError(ReceiptError.PhotoUnavailable()) },
        )
    return remember(camera, gallery) {
        ReceiptPickers(camera = camera, gallery = { gallery.launch() }, readingState = reading)
    }
}
