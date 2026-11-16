package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.Serializable

@Stable
@Serializable
internal sealed interface HomeDestination : NavDestination {
    @Stable @Serializable data object Home : HomeDestination

    @Stable @Serializable data object Feed : HomeDestination

    @Stable @Serializable data class Reminders(val compose: Boolean = false) : HomeDestination

    @Stable @Serializable data class ExpenseForm(val accountId: String) : HomeDestination
}
