package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class ExpenseAccountFormScreenViewModelContract : LoomViewModel() {
    data class Params(val accountId: String? = null)

    abstract val state: StateFlow<ExpenseAccountFormState>

    abstract fun onNameChanged(value: String)

    abstract fun onCurrencyChanged(value: String)

    abstract fun onParticipantToggled(id: String)

    abstract fun onSave()

    abstract fun onRetry()

    abstract fun onDismissError()
}
