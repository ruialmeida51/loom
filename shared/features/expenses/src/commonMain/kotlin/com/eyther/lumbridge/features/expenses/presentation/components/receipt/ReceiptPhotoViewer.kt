package com.eyther.lumbridge.features.expenses.presentation.components.receipt

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.eyther.lumbridge.core.presentation.components.LoomSecondaryButton
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.expenses.generated.resources.Res
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_zoom_in
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_zoom_out
import com.eyther.lumbridge.features.expenses.generated.resources.receipt_zoom_reset
import com.eyther.lumbridge.features.expenses.presentation.model.ReceiptPhotoUi
import org.jetbrains.compose.resources.stringResource

/** Accessible zoom/pan for a loaded receipt; the component owns only transient viewing state. */
@Composable
internal fun ReceiptPhotoViewer(photo: ReceiptPhotoUi, onInvalidPhoto: () -> Unit) {
    val state = remember(photo.id) { ReceiptZoomState() }
    val transform = rememberTransformableState { zoom, pan, _ ->
        state.transform(zoom = zoom, pan = pan)
    }
    val zoomIn = stringResource(Res.string.receipt_zoom_in)
    val zoomOut = stringResource(Res.string.receipt_zoom_out)
    Column(verticalArrangement = Arrangement.spacedBy(LoomSpacing.medium)) {
        Box(
            modifier =
                Modifier.weight(1f)
                    .fillMaxSize()
                    .clipToBounds()
                    .onSizeChanged(state::onSizeChanged)
                    .transformable(transform),
        ) {
            ReceiptImage(
                photo = photo,
                onInvalidPhoto = onInvalidPhoto,
                modifier =
                    Modifier.fillMaxSize().graphicsLayer {
                        scaleX = state.scale
                        scaleY = state.scale
                        translationX = state.offset.x
                        translationY = state.offset.y
                    },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
            FilledTonalIconButton(
                onClick = state::zoomOut,
                enabled = state.canZoomOut,
                modifier = Modifier.semantics { contentDescription = zoomOut },
            ) {
                Text("−")
            }
            FilledTonalIconButton(
                onClick = state::zoomIn,
                enabled = state.canZoomIn,
                modifier = Modifier.semantics { contentDescription = zoomIn },
            ) {
                Text("+")
            }
            LoomSecondaryButton(
                text = stringResource(Res.string.receipt_zoom_reset),
                onClick = state::reset,
                modifier = Modifier.weight(1f).testTag("receipt_zoom_reset"),
            )
        }
    }
}
