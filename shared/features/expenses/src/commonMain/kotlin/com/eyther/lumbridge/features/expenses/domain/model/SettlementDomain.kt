package com.eyther.lumbridge.features.expenses.domain.model

data class SettlementDomain(
    val id: String,
    val from: String,
    val to: String,
    val amountMinor: Long,
    val occurredOn: String,
)
