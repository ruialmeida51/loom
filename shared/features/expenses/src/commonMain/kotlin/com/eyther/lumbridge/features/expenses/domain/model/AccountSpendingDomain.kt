package com.eyther.lumbridge.features.expenses.domain.model

/**
 * What was spent in an account: how much each person paid (minor units, in the account's
 * currency) and how many expenses that was. Deleted expenses are not counted.
 */
data class AccountSpendingDomain(
    val paidByPerson: Map<String, Long> = emptyMap(),
    val expenseCount: Long = 0,
) {
    val totalMinor: Long = paidByPerson.values.sum()
}
