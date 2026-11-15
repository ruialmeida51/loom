package com.eyther.lumbridge.app.navigation.model

import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface HouseholdDestination : NavDestination {
    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Home")
    data object Home : HouseholdDestination

    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Shopping")
    data object Shopping : HouseholdDestination

    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Money")
    data object Balances : HouseholdDestination

    @Serializable data object More : HouseholdDestination
}
