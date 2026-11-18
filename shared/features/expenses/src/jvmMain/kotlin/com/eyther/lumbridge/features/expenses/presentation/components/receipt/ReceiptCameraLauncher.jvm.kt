package com.eyther.lumbridge.features.expenses.presentation.components.receipt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.PlatformFile

@Composable
internal actual fun rememberReceiptCameraLauncher(
    onResult: (PlatformFile?) -> Unit,
    onError: () -> Unit,
): ReceiptCameraLauncher = remember { ReceiptCameraLauncher(supported = false, launch = {}) }
