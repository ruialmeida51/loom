package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.*
import kotlinx.coroutines.flow.StateFlow

data class RecurringExpensesState(
    val rules: List<RecurringExpenseDomain> = emptyList(),
    val payerNames: Map<String, String> = emptyMap(),
    val isOwner: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
)

abstract class RecurringExpensesViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<RecurringExpensesState>

    abstract fun control(rule: RecurringExpenseDomain, action: RecurringExpenseActionDomain)

    abstract fun retry()

    abstract fun dismiss()
}
