package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class RecurringExpenseFormDomain(
    val rule: RecurringExpenseDomain,
    val account: ExpenseAccountDomain,
    val members: List<MemberDomain>,
    val actorIsOwner: Boolean,
)
