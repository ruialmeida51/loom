package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing

/**
 * Covers a read with placeholders while retaining the underlying composition, focus and geometry.
 */
@Composable
fun RefreshSkeletonContent(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    skeleton: @Composable () -> Unit = {
        Column(Modifier.padding(LoomSpacing.content)) { LoadingRows(rows = 6) }
    },
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.clipToBounds()) {
        Box(
            modifier =
                Modifier.graphicsLayer { alpha = if (isRefreshing) 0f else 1f }
                    .then(if (isRefreshing) Modifier.clearAndSetSemantics {} else Modifier),
        ) {
            content()
        }
        if (isRefreshing) {
            Surface(
                modifier =
                    Modifier.matchParentSize().testTag("refresh_skeleton").pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) awaitPointerEvent().changes.forEach { it.consume() }
                        }
                    },
                color = MaterialTheme.colorScheme.surface,
            ) {
                skeleton()
            }
        }
    }
}
