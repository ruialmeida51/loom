package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

data class ExpenseAccountFormState(
    val userId: String,
    val name: String = "",
    val currency: String = "EUR",
    val members: List<MemberDomain> = emptyList(),
    val selected: Set<String> = emptySet(),
    val account: ExpenseAccountDomain? = null,
    val loading: Boolean = true,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val savedId: String? = null,
) {
    val canSave: Boolean = !loading && !busy && name.isNotBlank() && userId in selected
}
