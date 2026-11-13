package com.eyther.lumbridge.core.navigation.animation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay

private const val DEFAULT_ANIMATION_DURATION_MS = 250

val fadeInTransitionSpec =
    fadeIn(animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS.toInt())) togetherWith
        fadeOut(animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS.toInt()))

val fadePopInTransitionSpec =
    fadeIn(animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS.toInt())) togetherWith
        fadeOut(animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS.toInt()))

val fadePredictivePopTransitionSpec =
    fadeIn(animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS.toInt())) togetherWith
        fadeOut(animationSpec = tween(DEFAULT_ANIMATION_DURATION_MS.toInt()))

val fadeInNavMetadata =
    NavDisplay.transitionSpec {
        fadeInTransitionSpec
    } +
        NavDisplay.popTransitionSpec {
            fadePopInTransitionSpec
        } +
        NavDisplay.predictivePopTransitionSpec {
            fadePredictivePopTransitionSpec
        }
