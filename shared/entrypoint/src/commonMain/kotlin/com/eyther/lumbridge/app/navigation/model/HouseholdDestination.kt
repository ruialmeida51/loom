package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
internal sealed interface HouseholdDestination : NavDestination {
    @Stable
    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Home")
    data object Home : HouseholdDestination

    @Stable
    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Shopping")
    data object Shopping : HouseholdDestination

    @Stable
    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Money")
    data object Balances : HouseholdDestination

    @Stable @Serializable data object Tasks : HouseholdDestination

    @Stable @Serializable data object More : HouseholdDestination
}
