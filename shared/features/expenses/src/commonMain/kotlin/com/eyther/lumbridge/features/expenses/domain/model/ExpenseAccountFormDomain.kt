package com.eyther.lumbridge.features.expenses.domain.model

import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseAccountFormDomain(
    val account: ExpenseAccountDomain?,
    val members: List<MemberDomain>,
    /** Whether this household can add guests; existing guests stay editable either way. */
    val guestsEnabled: Boolean = true,
)
