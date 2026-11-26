package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The small spinner an action shows in place of its control while it is saving (a row's menu, a
 * checkbox). Never for content loading: content uses skeletons.
 */
@Composable
fun ActionProgress(modifier: Modifier = Modifier, size: Dp = 20.dp) {
    CircularProgressIndicator(modifier = modifier.size(size), strokeWidth = 2.dp)
}
