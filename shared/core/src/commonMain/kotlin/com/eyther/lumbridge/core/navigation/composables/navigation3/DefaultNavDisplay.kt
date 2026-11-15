package com.eyther.lumbridge.core.navigation.composables.navigation3

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberUpdatedState
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

/** Keeps per-entry state outside tab transitions and provides opaque surfaces during push/pop. */
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
    val currentContent by rememberUpdatedState(content)
    // Decorators outlive root switches so each tab retains its own state and ViewModels.
    val entries = navigationState.toEntries { key ->
        NavEntry(
            key = key,
            metadata = metadata[key::class] ?: emptyMap(),
            content = { destination ->
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    currentContent(destination)
                }
            },
        )
    }
    // Switching tabs is not a push. Only destinations within the same stack slide.
    key(navigationState.activeRoot) {
        NavDisplay(
            modifier = modifier,
            entries = entries,
            onBack = navigator::goBack,
            transitionSpec = transitionSpec,
            popTransitionSpec = popTransitionSpec,
            predictivePopTransitionSpec = predictivePopTransitionSpec,
        )
    }
}
