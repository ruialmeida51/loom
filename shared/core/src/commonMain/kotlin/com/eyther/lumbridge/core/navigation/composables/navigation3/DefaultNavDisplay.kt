package com.eyther.lumbridge.core.navigation.composables.navigation3

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEvent
import com.eyther.lumbridge.core.navigation.animation.fadeInTransitionSpec
import com.eyther.lumbridge.core.navigation.animation.fadePopInTransitionSpec
import com.eyther.lumbridge.core.navigation.model.NavigationState
import com.eyther.lumbridge.core.navigation.model.Navigator
import com.eyther.lumbridge.core.navigation.model.toEntries
import kotlin.reflect.KClass

/**
 * Default NavDisplay implementation used by the app.
 *
 * This composable wraps `NavDisplay` from Navigation3 and wires it to the provided
 * `NavigationState` and `Navigator`. It renders the current backstack entries using the supplied
 * `content` lambda and applies a simple fade-in/fade-out transition for navigation changes.
 *
 * Parameters:
 * - [modifier]: optional Compose [Modifier] applied to the root [NavDisplay].
 * - [navigator]: the [Navigator] instance used to perform navigation actions such as back
 *   navigation.
 * - [navigationState]: the app's [NavigationState] describing the start route and back stacks.
 * - [transitionSpec]: a lambda that defines the transition animation for forward navigation.
 * - [popTransitionSpec]: a lambda that defines the transition animation for back navigation.
 * - [predictivePopTransitionSpec]: a lambda that defines the transition animation for predictive
 *   back navigation (e.g., swipe back gesture).
 * - [metadata]: a map of additional metadata for each destination, keyed by the destination's
 *   KClass.
 * - [content]: a composable lambda that receives a [NavKey] and should emit the UI for that
 *   destination.
 */
@Composable
fun DefaultNavDisplay(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    navigationState: NavigationState,
    transitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        fadeInTransitionSpec
    },
    popTransitionSpec: AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform = {
        fadePopInTransitionSpec
    },
    predictivePopTransitionSpec:
        AnimatedContentTransitionScope<Scene<NavKey>>.(
            @NavigationEvent.SwipeEdge Int,
        ) -> ContentTransform =
        {
            fadePopInTransitionSpec
        },
    metadata: Map<KClass<out NavKey>, Map<String, Any>> = emptyMap(),
    content: @Composable (key: NavKey) -> Unit,
) {
    NavDisplay(
        modifier = modifier,
        entries =
            navigationState.toEntries { key ->
                NavEntry(
                    key = key,
                    metadata = metadata[key::class] ?: emptyMap(),
                    content = content,
                )
            },
        onBack = navigator::goBack,
        transitionSpec = transitionSpec,
        popTransitionSpec = popTransitionSpec,
        predictivePopTransitionSpec = predictivePopTransitionSpec,
    )
}
