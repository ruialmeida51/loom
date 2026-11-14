package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.Serializable

@Stable
@Serializable
internal sealed interface HouseholdSetupDestination : NavDestination {
    @Stable
    @Serializable
    data object Select : HouseholdSetupDestination

    @Stable
    @Serializable
    data object Create : HouseholdSetupDestination

    @Stable
    @Serializable
    data object Join : HouseholdSetupDestination
}
