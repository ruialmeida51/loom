package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseFormDomain(
    val account: ExpenseAccountDomain,
    val members: List<MemberDomain>,
    val expense: ExpenseDomain?,
    /** Where a new expense can go instead; empty when editing, since an expense keeps its account. */
    val accounts: List<ExpenseAccountDomain> = emptyList(),
)
