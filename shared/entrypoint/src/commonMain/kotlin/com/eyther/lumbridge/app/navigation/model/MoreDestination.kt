package com.eyther.lumbridge.app.navigation.model

import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface MoreDestination : NavDestination {
    @Serializable data object Overview : MoreDestination

    @Serializable data object Reminders : MoreDestination

    @Serializable data object Feed : MoreDestination

    @Serializable data object HouseholdDetails : MoreDestination
}
