package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class PaymentDetailsScreenViewModelContract : LoomViewModel() {
    data class Params(val accountId: String, val paymentId: String)

    abstract val state: StateFlow<PaymentDetailsState>

    abstract fun onRefresh()

    abstract fun onContentChanged()

    abstract fun onDeleteRequested()

    abstract fun onDeleteCancelled()

    abstract fun onDeleteConfirmed()

    abstract fun onDismissError()
}
