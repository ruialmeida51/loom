package com.eyther.lumbridge.core.navigation.composables.navigation3

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import com.eyther.lumbridge.core.navigation.animation.slideInTransitionSpec
import com.eyther.lumbridge.core.navigation.animation.slidePopInTransitionSpec
import com.eyther.lumbridge.core.navigation.animation.slidePredictivePopTransitionSpec
import com.eyther.lumbridge.core.navigation.model.NavigationState
import com.eyther.lumbridge.core.navigation.model.Navigator
import kotlin.reflect.KClass

@Composable
fun HorizontalSlideNavDisplay(
    modifier: Modifier = Modifier,
    navigator: Navigator,
    navigationState: NavigationState,
    metadata: Map<KClass<out NavKey>, Map<String, Any>> = emptyMap(),
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable (key: NavKey) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = snackbarHost,
    ) {
        DefaultNavDisplay(
            modifier = Modifier.fillMaxSize(),
            navigator = navigator,
            navigationState = navigationState,
            transitionSpec = { slideInTransitionSpec },
            popTransitionSpec = { slidePopInTransitionSpec },
            predictivePopTransitionSpec = { slidePredictivePopTransitionSpec },
            metadata = metadata,
            content = content,
        )
    }
}
