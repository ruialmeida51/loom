package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.errors.ReceiptError
import kotlinx.coroutines.flow.StateFlow

abstract class ExpenseDetailsScreenViewModelContract : LoomViewModel() {
    data class Params(val accountId: String, val expenseId: String)

    abstract val state: StateFlow<ExpenseDetailsState>

    abstract fun onReceiptPicked(bytes: ByteArray)

    abstract fun onReceiptPickFailed(error: ReceiptError)

    abstract fun onReceiptDiscarded()

    abstract fun onReceiptRetry()

    abstract fun onReceiptSave()

    abstract fun onReceiptDeleteRequested()

    abstract fun onReceiptDeleteCancelled()

    abstract fun onReceiptDeleteConfirmed()

    abstract fun onContentChanged()

    abstract fun onRefresh()

    abstract fun onDeleteRequested()

    abstract fun onDeleteCancelled()

    abstract fun onDeleteConfirmed()

    abstract fun onDismissError()
}
