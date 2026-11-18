package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
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
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val width = with(density) { Indentation.toPx() }
    val threshold = with(density) { SwipeThreshold.toPx() }
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1f else 1f
    val committed = if (indented) width else 0f
    val gesture =
        remember(id, density) {
            ShoppingGroupingState(scope = scope, initialIndentation = committed)
        }
    LaunchedEffect(indented, width) { gesture.onCommitted(committed) }
    val nest by rememberUpdatedState(onNest)
    val unnest by rememberUpdatedState(onUnnest)
    val nestLabel = stringResource(Res.string.group_nest)
    val unnestLabel = stringResource(Res.string.group_unnest)
    Box(
        modifier =
            Modifier.testTag("shopping_group_$id")
                .padding(start = if (indented) Indentation else 0.dp)
                .graphicsLayer { translationX = direction * (gesture.position - committed) }
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
                .pointerInput(id, enabled, canNest, canUnnest, direction) {
                    if (!enabled || (!canNest && !canUnnest)) return@pointerInput
                    detectHorizontalDragGestures(
                        onDragStart = { gesture.onStart() },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            gesture.onDrag(
                                amount = amount * direction,
                                width = width,
                                canNest = canNest,
                                canUnnest = canUnnest,
                            )
                        },
                        onDragCancel = gesture::onCancel,
                        onDragEnd = {
                            when (
                                gesture.onFinish(
                                    threshold = threshold,
                                    width = width,
                                    canNest = canNest,
                                    canUnnest = canUnnest,
                                )
                            ) {
                                true -> nest()
                                false -> unnest()
                                null -> Unit
                            }
                        },
                    )
                },
    ) {
        content()
    }
}
