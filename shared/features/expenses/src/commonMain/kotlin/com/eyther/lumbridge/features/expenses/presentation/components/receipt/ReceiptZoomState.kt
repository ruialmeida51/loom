package com.eyther.lumbridge.features.expenses.presentation.components.receipt

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

private const val MAXIMUM_ZOOM = 5f // Enlargement for receipt text without unbounded scale.
private const val ZOOM_STEP = 1.5f // Shared increment for the accessible zoom controls.

/** Transient viewing geometry; never changes the photo or its feature-owned draft. */
@Stable
internal class ReceiptZoomState {
    var scale: Float by mutableFloatStateOf(1f)
        private set

    var offset: Offset by mutableStateOf(Offset.Zero)
        private set

    private var viewport: IntSize = IntSize.Zero

    val canZoomIn: Boolean
        get() = scale < MAXIMUM_ZOOM

    val canZoomOut: Boolean
        get() = scale > 1f

    fun onSizeChanged(size: IntSize) {
        viewport = size
        constrainOffset()
    }

    fun transform(zoom: Float, pan: Offset) {
        scale = (scale * zoom).coerceIn(1f, MAXIMUM_ZOOM)
        offset += pan
        constrainOffset()
    }

    fun zoomIn() {
        transform(zoom = ZOOM_STEP, pan = Offset.Zero)
    }

    fun zoomOut() {
        transform(zoom = 1f / ZOOM_STEP, pan = Offset.Zero)
    }

    fun reset() {
        scale = 1f
        offset = Offset.Zero
    }

    private fun constrainOffset() {
        val horizontal = viewport.width * (scale - 1f) / 2f
        val vertical = viewport.height * (scale - 1f) / 2f
        offset =
            Offset(
                x = offset.x.coerceIn(-horizontal, horizontal),
                y = offset.y.coerceIn(-vertical, vertical),
            )
    }
}
