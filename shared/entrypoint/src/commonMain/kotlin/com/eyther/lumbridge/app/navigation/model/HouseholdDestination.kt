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
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Feed")
    data object Feed : HouseholdDestination

    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Shopping")
    data object Shopping : HouseholdDestination

    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Money")
    data object Money : HouseholdDestination

    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Reminders")
    data object Reminders : HouseholdDestination

    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.RecurringExpenses")
    data object RecurringExpenses : HouseholdDestination

    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Insights")
    data object Insights : HouseholdDestination

    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.HouseholdDestination.Settings")
    data object Settings : HouseholdDestination
}
