package com.eyther.lumbridge.features.expenses.presentation.components.receipt

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.github.vinceglb.filekit.PlatformFile

@Stable internal class ReceiptCameraLauncher(val supported: Boolean, val launch: () -> Unit)

@Composable
internal expect fun rememberReceiptCameraLauncher(
    onResult: (PlatformFile?) -> Unit,
    onError: () -> Unit,
): ReceiptCameraLauncher
