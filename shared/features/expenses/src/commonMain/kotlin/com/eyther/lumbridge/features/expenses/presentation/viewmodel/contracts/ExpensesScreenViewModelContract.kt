package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class ExpensesScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<ExpensesState>

    abstract fun onTitleChanged(value: String)

    abstract fun onDateChanged(value: String)

    abstract fun onAmountChanged(value: String)

    abstract fun onPayerChanged(value: String)

    abstract fun onCategoryChanged(value: String)

    abstract fun onParticipantToggled(id: String)

    abstract fun onSplitModeChanged(value: String)

    abstract fun onCustomShareChanged(id: String, value: String)

    abstract fun onRecurringChanged(value: Boolean)

    abstract fun onAddSheetVisibilityChanged(value: Boolean)

    abstract fun onSettlementSheetVisibilityChanged(value: Boolean)

    abstract fun onFromMemberChanged(value: String)

    abstract fun onToMemberChanged(value: String)

    abstract fun onSettlementAmountChanged(value: String)

    abstract fun onSaveExpense()

    abstract fun onRecordSettlement()

    abstract fun onDismissError()
}
