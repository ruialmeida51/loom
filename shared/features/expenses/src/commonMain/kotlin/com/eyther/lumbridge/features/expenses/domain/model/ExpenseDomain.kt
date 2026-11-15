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
    val accountId: String = "",
    val version: Long = 0,
    val createdBy: String = "",
    val splitMethod: ExpenseSplitMethodDomain = ExpenseSplitMethodDomain.AMOUNTS,
    val splitValues: Map<String, Long> = emptyMap(),
) {
    fun canManage(userId: String, isOwner: Boolean): Boolean =
        userId == createdBy || userId == paidBy || isOwner
}
