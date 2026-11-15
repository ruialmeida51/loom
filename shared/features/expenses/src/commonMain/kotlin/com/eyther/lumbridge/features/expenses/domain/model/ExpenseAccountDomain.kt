package com.eyther.lumbridge.features.expenses.domain.model

data class ExpenseAccountDomain(
    val id: String,
    val name: String,
    val currency: String,
    val memberIds: Set<String>,
    val isDefault: Boolean = false,
    val archived: Boolean = false,
    val version: Long = 0,
    val createdBy: String = "",
) {
    fun canAddExpense(userId: String): Boolean = !archived && userId in memberIds

    fun canManage(userId: String, isOwner: Boolean): Boolean =
        !isDefault && (createdBy == userId || isOwner)
}
