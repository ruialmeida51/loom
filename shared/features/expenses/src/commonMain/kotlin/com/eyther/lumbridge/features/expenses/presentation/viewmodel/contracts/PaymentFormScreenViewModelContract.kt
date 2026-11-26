package com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts

import androidx.compose.runtime.Stable

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.expenses.domain.model.SuggestedSettlementDomain
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

abstract class PaymentFormScreenViewModelContract : LoomViewModel() {
    sealed interface RedirectTo {
        @Stable data object Saved : RedirectTo
    }

    data class Params(
        val accountId: String,
        val paymentId: String? = null,
        val suggestion: SuggestedSettlementDomain? = null,
    )

    abstract val state: StateFlow<PaymentFormState>

    abstract fun onFromChanged(value: String)

    abstract fun onToChanged(value: String)

    abstract fun onAmountChanged(value: String)

    abstract fun onDateChanged(value: LocalDate)

    abstract fun onSave()

    abstract fun onRetry()

    abstract fun onDismissError()

    abstract fun onNavigationHandled()
}
