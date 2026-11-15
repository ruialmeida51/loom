package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseActionDomain
import com.eyther.lumbridge.features.expenses.domain.model.RecurringExpenseDomain
import kotlinx.coroutines.flow.StateFlow

abstract class RecurringExpensesScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<RecurringExpensesState>

    abstract fun onControlSchedule(
        rule: RecurringExpenseDomain,
        action: RecurringExpenseActionDomain,
    )

    abstract fun onCancelRequested(rule: RecurringExpenseDomain)

    abstract fun onCancelDismissed()

    abstract fun onCancelConfirmed()

    abstract fun onRetry()

    abstract fun onDismissError()
}
