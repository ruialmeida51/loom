package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import androidx.compose.runtime.Stable

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseSplitSelectionDomain
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

abstract class RecurringExpenseFormScreenViewModelContract : LoomViewModel() {
    sealed interface RedirectTo {
        @Stable data object Saved : RedirectTo
    }

    data class Params(val accountId: String, val ruleId: String)

    abstract val state: StateFlow<RecurringExpenseFormState>

    abstract fun onTitleChanged(value: String)

    abstract fun onAmountChanged(value: String)

    abstract fun onPayerChanged(value: String)

    abstract fun onDateChanged(value: LocalDate)

    abstract fun onSplitConfirmed(value: ExpenseSplitSelectionDomain)

    abstract fun onSave()

    abstract fun onRetry()

    abstract fun onDismissError()

    abstract fun onNavigationHandled()
}
