package com.eyther.lumbridge.features.expenses.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseAccountFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SelectExpenseAccountUseCase
import com.eyther.lumbridge.features.expenses.presentation.mapper.toExpensesTextResource
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseAccountFormScreenViewModelContract
import com.eyther.lumbridge.features.expenses.presentation.viewmodel.contracts.ExpenseAccountFormState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ExpenseAccountFormScreenViewModel(
    private val params: ExpenseAccountFormScreenViewModelContract.Params,
    private val getExpenseAccountFormUseCase: GetExpenseAccountFormUseCase,
    private val saveExpenseAccountUseCase: SaveExpenseAccountUseCase,
    private val selectExpenseAccountUseCase: SelectExpenseAccountUseCase,
    private val context: HouseholdContextDomain,
) : ExpenseAccountFormScreenViewModelContract() {
    private data class InnerInputState(
        val name: String = "",
        val currency: String,
        val selected: Set<String> = emptySet(),
    )

    private val inputState = MutableStateFlow(InnerInputState(currency = context.currency))
    private val accountId = params.accountId ?: newId()
    private var initialized = false
    private val mutableState =
        MutableStateFlow(
                ExpenseAccountFormState(userId = context.userId, currency = context.currency),
            )
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(name = input.name, currency = input.currency, selected = input.selected)
                }
            }
            .launchWith(viewModelScope) { state -> loadInitialData(state = state) }
    override val state = mutableState.asStateFlow()

    override fun onNameChanged(value: String) {
        inputState.update { oldState -> oldState.copy(name = value) }
    }

    override fun onCurrencyChanged(value: String) {
        if (params.accountId == null)
            inputState.update { oldState -> oldState.copy(currency = value) }
    }

    override fun onParticipantToggled(id: String) {
        if (params.accountId != null || id == context.userId) return
        inputState.update { oldState ->
            oldState.copy(
                selected =
                    if (id in oldState.selected) oldState.selected - id else oldState.selected + id,
            )
        }
    }

    override fun onSave() {
        if (!state.value.canSave) return
        val input = inputState.value
        val before = state.value.account
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = safeCall {
                val account =
                    before?.copy(name = input.name)
                        ?: ExpenseAccountDomain(
                            id = accountId,
                            name = input.name,
                            currency = input.currency,
                            memberIds = input.selected,
                            createdBy = context.userId,
                        )
                val saved = saveExpenseAccountUseCase(account = account).getOrThrow()
                selectExpenseAccountUseCase(id = saved.id).getOrThrow()
                saved.id
            }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(busy = false, savedId = result.value)
                    is AppResult.Failure ->
                        oldState.copy(busy = false, error = result.error.toExpensesTextResource())
                }
            }
        }
    }

    override fun onRetry() {
        launch { loadInitialData(state = mutableState) }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null) }
    }

    private suspend fun loadInitialData(state: MutableStateFlow<ExpenseAccountFormState>) {
        when (val result = getExpenseAccountFormUseCase(id = params.accountId)) {
            is AppResult.Success -> {
                val data = result.value
                if (!initialized) {
                    inputState.value =
                        InnerInputState(
                            name = data.account?.name.orEmpty(),
                            currency = data.account?.currency ?: context.currency,
                            selected =
                                data.account?.memberIds ?: data.members.map { it.id }.toSet(),
                        )
                    initialized = true
                }
                state.update { oldState ->
                    oldState.copy(
                        account = data.account,
                        members = data.members,
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
