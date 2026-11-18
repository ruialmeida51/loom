package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import kotlinx.coroutines.flow.StateFlow

abstract class PaymentFormScreenViewModelContract : LoomViewModel() {
    data class Params(
        val accountId: String,
        val paymentId: String? = null,
        val suggestion: SuggestedSettlementDomain? = null,
    )

    abstract val state: StateFlow<PaymentFormState>

    abstract fun onFromChanged(value: String)

    abstract fun onToChanged(value: String)

    abstract fun onAmountChanged(value: String)

    abstract fun onDateChanged(value: String)

    abstract fun onSave()

    abstract fun onRetry()

    abstract fun onDismissError()
}
