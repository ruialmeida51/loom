package com.eyther.lumbridge.core.navigation.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/** The pushed destination covers the old one; a pop keeps the departing destination above it. */
internal fun forwardSlideTransition(direction: Int): ContentTransform =
    slideInHorizontally(
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        initialOffsetX = { it * direction },
    ) togetherWith
        slideOutHorizontally(
            animationSpec = tween(280, easing = FastOutSlowInEasing),
            targetOffsetX = { -it / 3 * direction },
        )

internal fun backSlideTransition(direction: Int): ContentTransform =
    (slideInHorizontally(
            animationSpec = tween(280, easing = FastOutSlowInEasing),
            initialOffsetX = { -it / 3 * direction },
        ) togetherWith
            slideOutHorizontally(
                animationSpec = tween(280, easing = FastOutSlowInEasing),
                targetOffsetX = { it * direction },
            ))
        .apply { targetContentZIndex = -1f }
