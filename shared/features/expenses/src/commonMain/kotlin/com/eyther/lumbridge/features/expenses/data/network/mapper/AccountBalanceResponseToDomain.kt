package com.eyther.lumbridge.features.expenses.data.network.mapper

import com.eyther.lumbridge.features.expenses.data.network.model.response.AccountBalanceResponse

/** Balances keyed by person, in a stable order so suggested repayments do not reshuffle. */
internal fun List<AccountBalanceResponse>.toBalances(): Map<String, Long> =
    sortedBy { it.userId }.associate { it.userId to it.balanceMinor }
