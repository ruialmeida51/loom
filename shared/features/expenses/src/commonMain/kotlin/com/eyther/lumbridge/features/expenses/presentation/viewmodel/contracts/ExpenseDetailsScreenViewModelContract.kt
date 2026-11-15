package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class ExpenseDetailsScreenViewModelContract : LoomViewModel() {
    data class Params(val accountId: String, val expenseId: String)

    abstract val state: StateFlow<ExpenseDetailsState>

    abstract fun onRefresh()

    abstract fun onDeleteRequested()

    abstract fun onDeleteCancelled()

    abstract fun onDeleteConfirmed()

    abstract fun onDismissError()
}
