package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.Serializable

@Stable
@Serializable
internal sealed interface PortalDestination : NavDestination {
    @Stable @Serializable data object Household : PortalDestination

    @Stable @Serializable data object Households : PortalDestination

    @Stable @Serializable data object Account : PortalDestination
}
