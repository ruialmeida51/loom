package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import kotlinx.coroutines.flow.StateFlow

abstract class RecurringExpenseFormScreenViewModelContract : LoomViewModel() {
    data class Params(val accountId: String, val ruleId: String)

    abstract val state: StateFlow<RecurringExpenseFormState>

    abstract fun onTitleChanged(value: String)

    abstract fun onAmountChanged(value: String)

    abstract fun onPayerChanged(value: String)

    abstract fun onDateChanged(value: String)

    abstract fun onSplitConfirmed(value: ExpenseSplitSelectionDomain)

    abstract fun onSave()

    abstract fun onRetry()

    abstract fun onDismissError()
}
