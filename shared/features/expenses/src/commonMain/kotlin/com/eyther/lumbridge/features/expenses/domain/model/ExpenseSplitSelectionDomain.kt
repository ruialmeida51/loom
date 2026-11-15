package com.eyther.lumbridge.features.expenses.domain.model

data class ExpenseSplitSelectionDomain(
    val method: ExpenseSplitMethodDomain,
    val participants: Set<String>,
    val values: Map<String, Long> = emptyMap(),
)
