package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.Serializable

@Stable
@Serializable
internal sealed interface FeedDestination : NavDestination {
    @Stable @Serializable data object Articles : FeedDestination

    @Stable @Serializable data object Subscriptions : FeedDestination
}
