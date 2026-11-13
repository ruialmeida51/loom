package com.eyther.lumbridge.features.expenses.domain.model

data class RecurringExpenseDomain(
    val id: String,
    val title: String,
    val amountMinor: Long,
    val paidBy: String,
    val category: String,
    val nextOn: String,
    val anchorDay: Int,
    val active: Boolean,
    val cancelled: Boolean,
    val createdBy: String,
    val version: Long,
    val shares: Map<String, Long>,
) {

    val status
        get() =
            if (cancelled) RecurringExpenseStatusDomain.CANCELLED
            else if (active) RecurringExpenseStatusDomain.ACTIVE
            else RecurringExpenseStatusDomain.PAUSED

    fun canManage(userId: String, isOwner: Boolean) =
        !cancelled && (isOwner || userId == paidBy || userId == createdBy)
}
