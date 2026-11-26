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
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseAccountIconDomain
import com.eyther.lumbridge.features.expenses.domain.model.ExpenseGuestDomain
import com.eyther.lumbridge.features.expenses.domain.usecase.GetExpenseAccountFormUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.RemoveAccountGuestUseCase
import com.eyther.lumbridge.features.expenses.domain.usecase.SaveAccountGuestUseCase
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
    private val saveAccountGuestUseCase: SaveAccountGuestUseCase,
    private val removeAccountGuestUseCase: RemoveAccountGuestUseCase,
    private val context: HouseholdContextDomain,
) : ExpenseAccountFormScreenViewModelContract() {
    private data class InnerInputState(
        val name: String = "",
        val currency: String,
        val icon: ExpenseAccountIconDomain = ExpenseAccountIconDomain.TRAVEL,
        val selected: Set<String> = emptySet(),
        val draftGuests: List<ExpenseGuestDomain> = emptyList(),
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
                    copy(
                        name = input.name,
                        currency = input.currency,
                        icon = input.icon,
                        selected = input.selected,
                        guests = account?.activeGuests ?: input.draftGuests,
                    )
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

    override fun onIconChanged(value: ExpenseAccountIconDomain) {
        inputState.update { oldState -> oldState.copy(icon = value) }
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

    override fun onGuestAdded(name: String) {
        if (!state.value.canAddGuest) return
        val guest = ExpenseGuestDomain(id = newId(), name = name.trim())
        val account = state.value.account
        if (account == null) {
            if (!guest.hasValidName()) return
            inputState.update { oldState ->
                oldState.copy(draftGuests = oldState.draftGuests + guest)
            }
            return
        }
        saveGuest(id = guest.id) { saveAccountGuestUseCase(account = account, guest = guest) }
    }

    override fun onGuestRenamed(id: String, name: String) {
        val account = state.value.account
        if (account == null) {
            inputState.update { oldState ->
                oldState.copy(
                    draftGuests =
                        oldState.draftGuests.map {
                            if (it.id == id && it.copy(name = name).hasValidName())
                                it.copy(name = name.trim())
                            else it
                        },
                )
            }
            return
        }
        val guest = account.guests.firstOrNull { it.id == id } ?: return
        if (guest.name == name.trim()) return
        saveGuest(id = id) {
            saveAccountGuestUseCase(account = account, guest = guest.copy(name = name))
        }
    }

    override fun onGuestRemoved(id: String) {
        val account = state.value.account
        if (account == null) {
            inputState.update { oldState ->
                oldState.copy(draftGuests = oldState.draftGuests.filterNot { it.id == id })
            }
            return
        }
        val guest = account.guests.firstOrNull { it.id == id } ?: return
        saveGuest(id = id) { removeAccountGuestUseCase(account = account, guest = guest) }
    }

    private fun saveGuest(id: String, call: suspend () -> AppResult<ExpenseAccountDomain>) {
        if (state.value.guestBusyId != null) return
        mutableState.update { oldState -> oldState.copy(guestBusyId = id, error = null) }
        launch {
            val result = call()
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            guestBusyId = null,
                            account = result.value,
                            guests = result.value.activeGuests,
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            guestBusyId = null,
                            error = result.error.toExpensesTextResource(),
                        )
                }
            }
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
                    before?.copy(name = input.name, icon = input.icon)
                        ?: ExpenseAccountDomain(
                            id = accountId,
                            name = input.name,
                            currency = input.currency,
                            memberIds = input.selected,
                            createdBy = context.userId,
                            icon = input.icon,
                        )
                val saved =
                    saveExpenseAccountUseCase(
                            account = account,
                            newGuests = if (before == null) input.draftGuests else emptyList(),
                        )
                        .getOrThrow()
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
                            icon = data.account?.displayIcon ?: ExpenseAccountIconDomain.TRAVEL,
                            selected =
                                data.account?.memberIds ?: data.members.map { it.id }.toSet(),
                        )
                    initialized = true
                }
                state.update { oldState ->
                    oldState.copy(
                        account = data.account,
                        members = data.members,
                        guests = data.account?.activeGuests ?: inputState.value.draftGuests,
                        guestsEnabled = data.guestsEnabled,
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
