package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.Serializable

@Stable
@Serializable
internal sealed interface ExpenseFormDestination : NavDestination {
    @Stable @Serializable data object Form : ExpenseFormDestination

    @Stable @Serializable data class Split(val amountMinor: Long) : ExpenseFormDestination
}
