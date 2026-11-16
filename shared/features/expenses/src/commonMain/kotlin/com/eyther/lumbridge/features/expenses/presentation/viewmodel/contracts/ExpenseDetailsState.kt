package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseDetailsState(
    val userId: String,
    val account: ExpenseAccountDomain? = null,
    val expense: ExpenseDomain? = null,
    val members: List<MemberDomain> = emptyList(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val showDeleteConfirmation: Boolean = false,
    val deleted: Boolean = false,
) {
    val isOwner: Boolean = members.firstOrNull { it.id == userId }?.role == "owner"
    val canEdit: Boolean =
        !busy &&
            account?.canAddExpense(userId = userId) == true &&
            expense?.canManage(userId = userId, isOwner = isOwner) == true
    val memberNames: Map<String, String> = members.associate { it.id to it.name }
}
