package com.eyther.lumbridge.app.navigation.model

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.navigation.model.NavDestination
import kotlinx.serialization.Serializable

@Stable
@Serializable
internal sealed interface BalancesDestination : NavDestination {
    @Stable @Serializable data object Overview : BalancesDestination

    @Stable @Serializable data object Recurring : BalancesDestination

    @Stable @Serializable data object Insights : BalancesDestination

    @Stable
    @Serializable
    data class Expense(val accountId: String, val expenseId: String) : BalancesDestination

    @Stable
    @Serializable
    data class Receipt(val accountId: String, val expenseId: String) : BalancesDestination

    @Stable
    @Serializable
    data class Form(val accountId: String, val expenseId: String? = null) : BalancesDestination

    @Stable
    @Serializable
    data class AccountForm(val accountId: String? = null) : BalancesDestination
}
