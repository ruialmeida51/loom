package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.features.shopping.generated.resources.Res
import com.eyther.lumbridge.features.shopping.generated.resources.group_nest
import com.eyther.lumbridge.features.shopping.generated.resources.group_unnest
import org.jetbrains.compose.resources.stringResource

private val Indentation = 24.dp // One hierarchy level without shrinking the checkbox touch target.
private val SwipeThreshold =
    56.dp // Require an intentional horizontal gesture before changing the group.

/** Only drag feedback is transient; the committed relationship belongs to the feature VM. */
@Composable
internal fun ShoppingGroupingGesture(
    id: String,
    indented: Boolean,
    canNest: Boolean,
    canUnnest: Boolean,
    enabled: Boolean,
    onNest: () -> Unit,
    onUnnest: () -> Unit,
    content: @Composable () -> Unit,
) {
    val indentation by animateDpAsState(if (indented) Indentation else 0.dp)
    val threshold = with(LocalDensity.current) { SwipeThreshold.toPx() }
    var distance by remember(id) { mutableFloatStateOf(0f) }
    val nest by rememberUpdatedState(onNest)
    val unnest by rememberUpdatedState(onUnnest)
    val nestLabel = stringResource(Res.string.group_nest)
    val unnestLabel = stringResource(Res.string.group_unnest)
    Box(
        modifier =
            Modifier.testTag("shopping_group_$id")
                .padding(start = indentation)
                .graphicsLayer { translationX = distance }
                .semantics {
                    customActions = buildList {
                        if (enabled && canNest)
                            add(
                                CustomAccessibilityAction(nestLabel) {
                                    nest()
                                    true
                                },
                            )
                        if (enabled && canUnnest)
                            add(
                                CustomAccessibilityAction(unnestLabel) {
                                    unnest()
                                    true
                                },
                            )
                    }
                }
                .pointerInput(id, enabled, canNest, canUnnest) {
                    if (!enabled || (!canNest && !canUnnest)) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { distance = 0f },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            distance =
                                (distance + amount).coerceIn(
                                    if (canUnnest) -threshold * 1.5f else 0f,
                                    if (canNest) threshold * 1.5f else 0f,
                                )
                        },
                        onDragCancel = { distance = 0f },
                        onDragEnd = {
                            if (distance >= threshold && canNest) nest()
                            else if (distance <= -threshold && canUnnest) unnest()
                            distance = 0f
                        },
                    )
                },
    ) {
        content()
    }
}
