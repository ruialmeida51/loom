package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/** Non-interactive row placeholders for a first read; refreshed content stays on screen. */
@Composable
fun LoadingRows(modifier: Modifier = Modifier, rows: Int = 3) {
    Column(modifier = modifier.testTag("loading_rows")) { repeat(rows) { LoadingRow() } }
}
