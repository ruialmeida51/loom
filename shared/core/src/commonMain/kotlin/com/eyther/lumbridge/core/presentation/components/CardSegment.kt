package com.eyther.lumbridge.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CardSegmentRadius = 24.dp

/** The outer shape of one row of a segmented card: only the ends round their corners. */
fun cardSegmentShape(first: Boolean, last: Boolean, radius: Dp = CardSegmentRadius): Shape =
    RoundedCornerShape(
        topStart = if (first) radius else 0.dp,
        topEnd = if (first) radius else 0.dp,
        bottomStart = if (last) radius else 0.dp,
        bottomEnd = if (last) radius else 0.dp,
    )

/**
 * Paints one row of a card that a lazy list renders row by row: rows share the card surface and
 * only the [first] and [last] rows round their outer corners. When the list spaces its items,
 * pass that spacing as [gap]: the surface then bridges it and a hairline divider marks the seam.
 */
@Composable
fun Modifier.cardSegment(
    first: Boolean,
    last: Boolean,
    gap: Dp = 0.dp,
    radius: Dp = CardSegmentRadius,
): Modifier {
    val shape = cardSegmentShape(first = first, last = last, radius = radius)
    val color = MaterialTheme.colorScheme.surfaceContainerLow
    if (gap == 0.dp) return this.clip(shape).background(color)
    val dividerColor = MaterialTheme.colorScheme.outlineVariant
    return this.drawBehind {
        val bridge = if (last) 0f else gap.toPx()
        val outline =
            shape.createOutline(
                size = Size(size.width, size.height + bridge),
                layoutDirection = layoutDirection,
                density = this,
            )
        drawOutline(outline = outline, color = color)
        if (!last) {
            val y = size.height + bridge / 2
            val inset = 16.dp.toPx()
            drawLine(
                color = dividerColor,
                start = Offset(inset, y),
                end = Offset(size.width - inset, y),
                strokeWidth = 1.dp.toPx(),
            )
        }
    }
}
