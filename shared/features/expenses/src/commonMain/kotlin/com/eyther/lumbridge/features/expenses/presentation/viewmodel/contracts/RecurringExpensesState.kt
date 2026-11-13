package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain

data class RecurringExpensesState(
    val rules: List<RecurringExpenseDomain> = emptyList(),
    val payerNames: Map<String, String> = emptyMap(),
    val role: String? = null,
    val busy: Boolean = false,
    val error: TextResource? = null,
) {

    val isOwner: Boolean = role == "owner"
}
