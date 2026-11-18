package com.eyther.lumbridge.features.shopping.presentation.components

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val SETTLE_DURATION_MILLIS = 140

/**
 * One draw offset for dragging, committing and rollback; layout never animates against the finger.
 */
@Stable
internal class ShoppingGroupingState(
    private val scope: CoroutineScope,
    initialIndentation: Float,
) {
    var position: Float by mutableFloatStateOf(initialIndentation)
        private set

    private var animation: Job? = null
    private var dragging: Boolean = false
    private var distance: Float = 0f
    private var committed: Float = initialIndentation
    private var start: Float = initialIndentation

    fun onCommitted(indentation: Float) {
        committed = indentation
        if (!dragging) settle(indentation)
    }

    fun onStart() {
        animation?.cancel()
        dragging = true
        distance = 0f
        start = position
    }

    fun onDrag(amount: Float, width: Float, canNest: Boolean, canUnnest: Boolean) {
        distance += amount
        position =
            (start + distance).coerceIn(
                minimumValue = if (canUnnest) 0f else start,
                maximumValue = if (canNest) width else start,
            )
    }

    /** A non-null result requests the next relationship; the feature VM owns persistence. */
    fun onFinish(threshold: Float, width: Float, canNest: Boolean, canUnnest: Boolean): Boolean? {
        dragging = false
        val target =
            when {
                canNest && distance >= threshold -> true
                canUnnest && distance <= -threshold -> false
                else -> null
            }
        settle(
            when (target) {
                true -> width
                false -> 0f
                null -> committed
            },
        )
        return target
    }

    fun onCancel() {
        if (!dragging) return
        dragging = false
        settle(committed)
    }

    private fun settle(target: Float) {
        animation?.cancel()
        if (position == target) return
        animation = scope.launch {
            animate(
                initialValue = position,
                targetValue = target,
                animationSpec = tween(SETTLE_DURATION_MILLIS),
            ) { value, _ ->
                position = value
            }
        }
    }
}
