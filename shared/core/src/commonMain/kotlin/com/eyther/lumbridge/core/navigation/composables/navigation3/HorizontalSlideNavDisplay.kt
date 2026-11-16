package com.eyther.lumbridge.core.navigation.composables.navigation3

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation3.runtime.NavKey
import com.eyther.lumbridge.core.navigation.animation.backSlideTransition
import com.eyther.lumbridge.core.navigation.animation.forwardSlideTransition
import com.eyther.lumbridge.core.navigation.model.NavigationState
import com.eyther.lumbridge.core.navigation.model.Navigator
import kotlin.reflect.KClass

@Composable
fun HorizontalSlideNavDisplay(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    navigationState: NavigationState,
    metadata: Map<KClass<out NavKey>, Map<String, Any>> = emptyMap(),
    content: @Composable (key: NavKey) -> Unit,
) {
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Ltr) 1 else -1
    DefaultNavDisplay(
        modifier = modifier,
        navigator = navigator,
        navigationState = navigationState,
        transitionSpec = { forwardSlideTransition(direction = direction) },
        popTransitionSpec = { backSlideTransition(direction = direction) },
        predictivePopTransitionSpec = { backSlideTransition(direction = direction) },
        metadata = metadata,
        content = content,
    )
}
