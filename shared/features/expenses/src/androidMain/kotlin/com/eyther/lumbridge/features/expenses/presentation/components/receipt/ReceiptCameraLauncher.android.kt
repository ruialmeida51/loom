package com.eyther.lumbridge.features.expenses.presentation.components.receipt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher

@Composable
internal actual fun rememberReceiptCameraLauncher(
    onResult: (PlatformFile?) -> Unit,
    onError: () -> Unit,
): ReceiptCameraLauncher {
    val launcher = rememberCameraPickerLauncher(onError = { onError() }, onResult = onResult)
    return remember(launcher) {
        ReceiptCameraLauncher(supported = true, launch = { launcher.launch() })
    }
}
