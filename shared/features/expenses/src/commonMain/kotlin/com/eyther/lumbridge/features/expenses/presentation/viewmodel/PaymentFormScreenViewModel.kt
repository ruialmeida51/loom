package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.model.MoneyDomain
import com.eyther.lumbridge.features.expenses.domain.model.SettlementDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetSettlementFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RecordSettlementUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveSettlementUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.PaymentFormState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Owns one payment draft; opening a date picker or resuming the app cannot replace its inputs. */
class PaymentFormScreenViewModel(
    private val params: PaymentFormScreenViewModelContract.Params,
    private val getSettlementFormUseCase: GetSettlementFormUseCase,
    private val recordSettlementUseCase: RecordSettlementUseCase,
    private val saveSettlementUseCase: SaveSettlementUseCase,
    private val context: HouseholdContextDomain,
) : PaymentFormScreenViewModelContract() {
    private data class InnerInputState(
        val from: String,
        val to: String = "",
        val amount: String = "",
        val occurredOn: String,
    )

    private val paymentId: String = params.paymentId ?: newId()
    private var initialized: Boolean = false
    private val inputState =
        MutableStateFlow(
            InnerInputState(from = context.userId, occurredOn = todayIso(context.timeZone)),
        )
    private val mutableState =
        MutableStateFlow(
                PaymentFormState(userId = context.userId),
            )
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(
                        from = input.from,
                        to = input.to,
                        amount = input.amount,
                        occurredOn = input.occurredOn,
                    )
                }
            }
            .launchWith(viewModelScope) { state -> loadForm(state = state) }
    override val state = mutableState.asStateFlow()

    override fun onFromChanged(value: String) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(from = value) }
    }

    override fun onToChanged(value: String) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(to = value) }
    }

    override fun onAmountChanged(value: String) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(amount = value) }
    }

    override fun onDateChanged(value: String) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(occurredOn = value) }
    }

    override fun onSave() {
        if (!state.value.canSave) return
        val account = state.value.account ?: return
        val input = inputState.value
        val before = state.value.payment
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = safeCall {
                val payment =
                    SettlementDomain(
                        id = paymentId,
                        from = input.from,
                        to = input.to,
                        amountMinor =
                            MoneyDomain.parse(text = input.amount, currency = account.currency)
                                .minorUnits,
                        occurredOn = input.occurredOn,
                        accountId = account.id,
                        version = before?.version ?: 0,
                        createdBy = before?.createdBy ?: context.userId,
                    )
                if (before == null) recordSettlementUseCase(settlement = payment).getOrThrow()
                else saveSettlementUseCase(settlement = payment).getOrThrow()
            }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(busy = false, saved = true)
                    is AppResult.Failure ->
                        oldState.copy(busy = false, error = result.error.toExpensesTextResource())
                }
            }
        }
    }

    override fun onRetry() {
        if (state.value.loading || state.value.busy) return
        mutableState.update { oldState -> oldState.copy(loading = true, error = null) }
        launch { loadForm(state = mutableState, refreshReferences = true) }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null) }
    }

    private suspend fun loadForm(
        state: MutableStateFlow<PaymentFormState>,
        refreshReferences: Boolean = false,
    ) {
        val result = delayAtLeast {
            getSettlementFormUseCase(
                accountId = params.accountId,
                settlementId = params.paymentId,
                refreshReferences = refreshReferences,
            )
        }
        when (result) {
            is AppResult.Success -> {
                val data = result.value
                if (!initialized) {
                    val payment = data.settlement
                    val suggestion = params.suggestion
                    inputState.update { oldState ->
                        oldState.copy(
                            from = payment?.from ?: suggestion?.from ?: context.userId,
                            to =
                                payment?.to
                                    ?: suggestion?.to
                                    ?: data.members
                                        .firstOrNull { it.id != context.userId }
                                        ?.id
                                        .orEmpty(),
                            amount =
                                (payment?.amountMinor ?: suggestion?.amountMinor)
                                    ?.let {
                                        MoneyDomain(
                                                minorUnits = it,
                                                currency = data.account.currency,
                                            )
                                            .inputText()
                                    }
                                    .orEmpty(),
                            occurredOn = payment?.occurredOn ?: oldState.occurredOn,
                        )
                    }
                    initialized = true
                }
                state.update { oldState ->
                    oldState.copy(
                        account = data.account,
                        payment = oldState.payment ?: data.settlement,
                        members = data.members.toImmutableList(),
                        loading = false,
                        error = null,
                    )
                }
            }
            is AppResult.Failure ->
                state.update { oldState ->
                    oldState.copy(loading = false, error = result.error.toExpensesTextResource())
                }
        }
    }
}
