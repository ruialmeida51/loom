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
    val accountId: String = "",
    val splitMethod: ExpenseSplitMethodDomain = ExpenseSplitMethodDomain.AMOUNTS,
    val splitValues: Map<String, Long> = shares,
) {

    val status: RecurringExpenseStatusDomain =
        when {
            cancelled -> RecurringExpenseStatusDomain.CANCELLED
            active -> RecurringExpenseStatusDomain.ACTIVE
            else -> RecurringExpenseStatusDomain.PAUSED
        }

    fun canManage(userId: String, isOwner: Boolean): Boolean =
        !cancelled && canDelete(userId = userId, isOwner = isOwner)

    fun canDelete(userId: String, isOwner: Boolean): Boolean =
        isOwner || userId == paidBy || userId == createdBy

    val split: ExpenseSplitSelectionDomain
        get() =
            ExpenseSplitSelectionDomain(
                method = splitMethod,
                participants = shares.keys,
                values = splitValues,
            )
}
