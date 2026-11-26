package com.eyther.lumbridge.features.expenses.domain.model

import kotlinx.datetime.LocalDate

data class ExpenseDomain(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val paidBy: String,
    val category: String,
    val occurredOn: LocalDate,
    val shares: Map<String, Long>,
    val recurring: Boolean = false,
    val accountId: String = "",
    val version: Long = 0,
    val createdBy: String = "",
    val splitMethod: ExpenseSplitMethodDomain = ExpenseSplitMethodDomain.AMOUNTS,
    val splitValues: Map<String, Long> = emptyMap(),
    /** ISO-8601 instant the record was entered; empty until the server stores it. */
    val createdAt: String = "",
    /** Set when the expense was paid in another currency; [amountMinor] is then its conversion. */
    val original: OriginalAmountDomain? = null,
) {
    fun canManage(userId: String, isOwner: Boolean): Boolean =
        userId == createdBy || userId == paidBy || isOwner
}
