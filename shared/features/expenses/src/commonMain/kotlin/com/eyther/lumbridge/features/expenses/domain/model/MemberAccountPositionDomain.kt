package com.eyther.lumbridge.features.expenses.domain.model

/** One account's position; currencies are never combined in departure confirmations. */
data class MemberAccountPositionDomain(
    val accountId: String,
    val accountName: String,
    val isDefault: Boolean,
    val currency: String,
    val balanceMinor: Long,
)
