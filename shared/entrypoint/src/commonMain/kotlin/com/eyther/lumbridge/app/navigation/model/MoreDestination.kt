package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
internal sealed interface MoreDestination : NavDestination {
    @Stable @Serializable data object Overview : MoreDestination

    /** Compatibility for saved More navigation from builds before the Tasks tab. */
    @Stable
    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.model.MoreDestination.Reminders")
    data object LegacyReminders : MoreDestination

    @Stable @Serializable data object Feed : MoreDestination

    @Stable @Serializable data object HouseholdDetails : MoreDestination

    /** A housemate's profile, opened from household details. */
    @Stable @Serializable data class MemberProfile(val memberId: String) : MoreDestination
}
