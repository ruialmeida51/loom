package com.eyther.lumbridge.core.navigation.animation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.navigation3.ui.NavDisplay

// region Slide In Animations

val slideInTransitionSpec =
    slideInHorizontally(initialOffsetX = { it }) togetherWith
        slideOutHorizontally(targetOffsetX = { -it })

val slidePopInTransitionSpec =
    slideInHorizontally(initialOffsetX = { -it }) togetherWith
        slideOutHorizontally(targetOffsetX = { it })

val slidePredictivePopTransitionSpec =
    slideInHorizontally(initialOffsetX = { -it }) togetherWith
        slideOutHorizontally(targetOffsetX = { it })

val slideInNavMetadata =
    NavDisplay.transitionSpec {
        slideInTransitionSpec
    } +
        NavDisplay.popTransitionSpec {
            slidePopInTransitionSpec
        } +
        NavDisplay.predictivePopTransitionSpec {
            slidePredictivePopTransitionSpec
        }

// endregion Slide In Animations

// region Slide Up Animations
val slideUpTransitionSpec =
    slideInVertically(initialOffsetY = { it }) togetherWith
        slideOutVertically(targetOffsetY = { -it })

val slidePopUpTransitionSpec =
    slideInVertically(initialOffsetY = { -it }) togetherWith
        slideOutVertically(targetOffsetY = { it })

// no anim
val slidePredictivePopUpTransitionSpec =
    slideInVertically(initialOffsetY = { -it }) togetherWith
        slideOutVertically(targetOffsetY = { it })

val slideUpNavMetadata =
    NavDisplay.transitionSpec {
        slideUpTransitionSpec
    } +
        NavDisplay.popTransitionSpec {
            slidePopUpTransitionSpec
        } +
        NavDisplay.predictivePopTransitionSpec {
            slidePredictivePopUpTransitionSpec
        }

// endregion
