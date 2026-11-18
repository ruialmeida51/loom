package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class ReceiptViewerScreenViewModelContract : LoomViewModel() {
    data class Params(val expenseId: String, val accountId: String)

    abstract val state: StateFlow<ReceiptViewerState>

    abstract fun onRetry()

    abstract fun onInvalidPhoto()
}
