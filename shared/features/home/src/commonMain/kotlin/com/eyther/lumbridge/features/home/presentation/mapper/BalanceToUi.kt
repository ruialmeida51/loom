package com.eyther.lumbridge.features.home.presentation.mapper

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.model.HouseholdLedgerDomain
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.home.presentation.model.HomeBalanceStatusUi
import com.eyther.lumbridge.features.home.presentation.model.HomeBalanceUi
import com.eyther.lumbridge.features.home.presentation.model.HomeCounterpartyUi
import kotlinx.collections.immutable.toImmutableList

private const val HOME_COUNTERPARTIES = 3

/** Summarises [balances] for [userId], naming the people they settle with first. */
internal fun homeBalanceUi(
    balances: Map<String, Long>,
    userId: String,
    currency: String,
    memberNames: Map<String, String>,
): HomeBalanceUi {
    val own = balances[userId] ?: 0L
    val counterparties =
        (safeCall { HouseholdLedgerDomain.suggestions(balances) } as? AppResult.Success)
            ?.value
            .orEmpty()
            .filter { it.from == userId || it.to == userId }
            .map { suggestion ->
                val owesYou = suggestion.to == userId
                val other = if (owesYou) suggestion.from else suggestion.to
                HomeCounterpartyUi(
                    memberId = other,
                    name = memberNames[other]?.ifBlank { null },
                    amount = MoneyDomain(suggestion.amountMinor, currency).formattedMagnitude(),
                    owesYou = owesYou,
                )
            }
            .take(HOME_COUNTERPARTIES)
            .toImmutableList()
    return HomeBalanceUi(
        status =
            when {
                own > 0 -> HomeBalanceStatusUi.OWED
                own < 0 -> HomeBalanceStatusUi.OWES
                else -> HomeBalanceStatusUi.SETTLED
            },
        amount = MoneyDomain(own, currency).formattedMagnitude(),
        counterparties = counterparties,
    )
}
