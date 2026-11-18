package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RemoveSettlementUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentDetailsScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentDetailsState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Reads one payment independently of the overview and freezes a confirmed deletion's version. */
class PaymentDetailsScreenViewModel(
    private val params: PaymentDetailsScreenViewModelContract.Params,
    private val getSettlementFormUseCase: GetSettlementFormUseCase,
    private val removeSettlementUseCase: RemoveSettlementUseCase,
    context: HouseholdContextDomain,
) : PaymentDetailsScreenViewModelContract() {
    private val mutableState =
        MutableStateFlow(PaymentDetailsState(userId = context.userId)).launchWith(viewModelScope) {
            state ->
            loadDetails(state = state, transition = true)
        }
    override val state = mutableState.asStateFlow()
    private var reading: Boolean = false

    override fun onRefresh() {
        if (
            reading ||
                state.value.loading ||
                state.value.busy ||
                state.value.deletingPayment != null
        )
            return
        mutableState.update { oldState -> oldState.copy(refreshing = true, error = null) }
        launch { loadDetails(state = mutableState, transition = true) }
    }

    override fun onContentChanged() {
        if (
            reading ||
                state.value.loading ||
                state.value.busy ||
                state.value.deletingPayment != null
        )
            return
        launch { loadDetails(state = mutableState, transition = false) }
    }

    override fun onDeleteRequested() {
        if (!state.value.canEdit || state.value.deletingPayment != null) return
        mutableState.update { oldState ->
            oldState.copy(deletingPayment = oldState.payment, deleteError = null)
        }
    }

    override fun onDeleteCancelled() {
        if (state.value.busy) return
        mutableState.update { oldState ->
            oldState.copy(deletingPayment = null, deleteError = null)
        }
    }

    override fun onDeleteConfirmed() {
        if (state.value.busy) return
        val target = state.value.deletingPayment ?: return
        mutableState.update { oldState -> oldState.copy(busy = true, deleteError = null) }
        launch {
            val result = removeSettlementUseCase(settlement = target)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(busy = false, deleted = true, deletingPayment = null)
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            deleteError = result.error.toExpensesTextResource(),
                        )
                }
            }
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null) }
    }

    private suspend fun loadDetails(
        state: MutableStateFlow<PaymentDetailsState>,
        transition: Boolean,
    ) {
        reading = true
        try {
            val result =
                delayAtLeast(timeToWaitInMillis = if (transition) 300L else 0L) {
                    getSettlementFormUseCase(
                        accountId = params.accountId,
                        settlementId = params.paymentId,
                        refreshReferences = true,
                    )
                }
            state.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            account = result.value.account,
                            payment = result.value.settlement,
                            members = result.value.members.toImmutableList(),
                            loading = false,
                            refreshing = false,
                            error = null,
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            loading = false,
                            refreshing = false,
                            error = result.error.toExpensesTextResource(),
                        )
                }
            }
        } finally {
            reading = false
        }
    }
}
