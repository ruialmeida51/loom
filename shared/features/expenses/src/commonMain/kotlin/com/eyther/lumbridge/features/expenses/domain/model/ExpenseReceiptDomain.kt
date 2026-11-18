package com.eyther.lumbridge.features.expenses.domain.model

/** Removal retains the version token so a stale operation cannot delete a later photo. */
data class ExpenseReceiptDomain(
    val expenseId: String,
    val accountId: String,
    val objectPath: String? = null,
    val version: Long = 0,
)
