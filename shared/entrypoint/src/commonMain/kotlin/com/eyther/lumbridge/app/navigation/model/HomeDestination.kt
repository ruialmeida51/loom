package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Stable
@Serializable
internal sealed interface HomeDestination : NavDestination {
    @Stable @Serializable data object Home : HomeDestination

    @Stable @Serializable data object Feed : HomeDestination

    /** Restores pre-Tasks saved navigation; the Host redirects to the tab or quick-add sheet. */
    @Stable
    @Serializable
    @SerialName("com.eyther.lumbridge.app.navigation.model.HomeDestination.Reminders")
    data class LegacyReminders(val compose: Boolean = false) : HomeDestination

    @Stable @Serializable data class ExpenseForm(val accountId: String) : HomeDestination
}
