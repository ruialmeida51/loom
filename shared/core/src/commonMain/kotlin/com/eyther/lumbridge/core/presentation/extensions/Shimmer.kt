package com.eyther.lumbridge.core.presentation.extensions

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline

private const val SHIMMER_DURATION_IN_MILLIS = 1200
private const val SHIMMER_LABEL = "shimmer"

/**
 * Applies a shimmering skeleton-loading effect that moves in a single direction.
 *
 * The shimmer is an animated [Brush.linearGradient] that moves across the composable. It uses
 * [RepeatMode.Restart] over `-1.5f..2.5f` so the gradient starts and ends fully off-screen,
 * producing a seamless loop.
 *
 * Ideal for list placeholders and typical skeleton UIs.
 *
 * @param shouldShimmer Whether to shimmer. When false the receiver is returned untouched: no
 *   animation is started and nothing is added to the modifier chain.
 * @param shape A capsule by default, like every non-container placeholder. Container-sized
 *   skeletons pass the container's rounded shape.
 * @return A [Modifier] that draws the shimmer in place of the content.
 */
@Composable
fun Modifier.shimmer(
    shouldShimmer: Boolean,
    shape: Shape = CircleShape,
): Modifier {
    if (!shouldShimmer) return this

    return shimmerOverlay(
        progress =
            rememberShimmerProgress(
                initialValue = -1.5f,
                targetValue = 2.5f,
                repeatMode = RepeatMode.Restart,
            ),
        shape = shape,
    )
}

/**
 * Applies a bidirectional shimmering effect that moves back and forth continuously.
 *
 * The gradient's horizontal offset oscillates between 0f and 1f using [RepeatMode.Reverse], giving
 * a "breathing light" effect that reverses at each edge instead of restarting. Smoother than the
 * traditional sweep on compact or static components.
 *
 * @param shouldShimmer Whether to shimmer. When false the receiver is returned untouched: no
 *   animation is started and nothing is added to the modifier chain.
 * @param shape As for [shimmer].
 * @return A [Modifier] that draws the shimmer in place of the content.
 */
@Composable
fun Modifier.shimmerReverse(
    shouldShimmer: Boolean,
    shape: Shape = CircleShape,
): Modifier {
    if (!shouldShimmer) return this

    return shimmerOverlay(
        progress =
            rememberShimmerProgress(
                initialValue = 0f,
                targetValue = 1f,
                repeatMode = RepeatMode.Reverse,
            ),
        shape = shape,
    )
}

/** Read animation state in drawing, so a shimmer frame does not recompose its surrounding list. */
@Composable
private fun Modifier.shimmerOverlay(
    progress: State<Float>,
    shape: Shape,
): Modifier {
    val colours = rememberShimmerColours()

    return drawWithCache {
        drawShimmer(
            progress = progress,
            colours = colours,
            outline = shape.createOutline(size, layoutDirection, this),
            width = size.width,
            height = size.height,
        )
    }
}

private fun CacheDrawScope.drawShimmer(
    progress: State<Float>,
    colours: List<Color>,
    outline: Outline,
    width: Float,
    height: Float,
): DrawResult = onDrawWithContent {
    val startX = width * progress.value

    drawOutline(
        outline = outline,
        brush =
            Brush.linearGradient(
                colors = colours,
                start = Offset(x = startX, y = 0f),
                end = Offset(x = startX + width, y = height),
            ),
    )
}

@Composable
private fun rememberShimmerProgress(
    initialValue: Float,
    targetValue: Float,
    repeatMode: RepeatMode,
): State<Float> {
    val transition = rememberInfiniteTransition(label = SHIMMER_LABEL)

    return transition.animateFloat(
        initialValue = initialValue,
        targetValue = targetValue,
        animationSpec =
            infiniteRepeatable(
                animation = tween(SHIMMER_DURATION_IN_MILLIS, easing = LinearEasing),
                repeatMode = repeatMode,
            ),
        label = SHIMMER_LABEL,
    )
}

@Composable
private fun rememberShimmerColours(): List<Color> {
    val base = MaterialTheme.colorScheme.onSurfaceVariant

    return remember(base) {
        listOf(
            base.copy(alpha = 0.3f),
            base.copy(alpha = 0.1f),
            base.copy(alpha = 0.3f),
        )
    }
}
