package com.eyther.lumbridge.features.expenses.domain.model

data class ExpenseDomain(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val paidBy: String,
    val category: String,
    val occurredOn: String,
    val shares: Map<String, Long>,
    val recurring: Boolean = false,
)
