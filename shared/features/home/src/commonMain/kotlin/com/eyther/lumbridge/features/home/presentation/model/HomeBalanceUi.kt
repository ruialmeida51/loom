package com.eyther.lumbridge.features.home.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/** Whether the user is owed, owes, or is settled on the household's everyday account. */
enum class HomeBalanceStatusUi {
    OWED,
    OWES,
    SETTLED,
}

/**
 * One person the user settles with: [owesYou] tells the direction, [amount] is formatted, and a null
 * [name] means a former member.
 */
@Immutable
data class HomeCounterpartyUi(
    val memberId: String,
    val name: String?,
    val amount: String,
    val owesYou: Boolean,
)

@Immutable
data class HomeBalanceUi(
    val status: HomeBalanceStatusUi,
    val amount: String,
    val counterparties: ImmutableList<HomeCounterpartyUi>,
)
