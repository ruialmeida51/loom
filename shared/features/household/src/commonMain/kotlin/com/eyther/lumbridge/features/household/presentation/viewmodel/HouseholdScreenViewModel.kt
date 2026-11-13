package com.eyther.lumbridge.features.household.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdState
import kotlinx.coroutines.flow.*

class HouseholdScreenViewModel(
    private val get: GetHouseholdsUseCase,
    private val create: CreateHouseholdUseCase,
    private val join: JoinHouseholdUseCase,
) : HouseholdScreenViewModelContract() {
    private data class InnerInputState(
        val name: String = "",
        val token: String = "",
        val currency: String = "EUR",
    )

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(HouseholdState()).observeWith(viewModelScope) { state ->
            inputState.updateState(state) {
                copy(name = it.name, token = it.token, currency = it.currency)
            }
        }
    override val state = mutableState.asStateFlow()

    init {
        refresh()
    }

    override fun name(value: String) {
        inputState.update { it.copy(name = value) }
    }

    override fun token(value: String) {
        inputState.update { it.copy(token = value) }
    }

    override fun currency(value: String) {
        inputState.update { it.copy(currency = value) }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }

    override fun refresh() {
        launch {
            when (val result = get()) {
                is AppResult.Success ->
                    mutableState.update { it.copy(items = result.value, busy = false) }
                is AppResult.Failure ->
                    mutableState.update { it.copy(error = result.message, busy = false) }
            }
        }
    }

    override fun create() {
        if (mutableState.value.busy) return
        val input = inputState.value
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = create(input.name, input.currency)) {
                is AppResult.Success -> {
                    inputState.update { it.copy(name = "") }
                    mutableState.update { it.copy(name = "") }
                    refresh()
                }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    override fun join() {
        if (mutableState.value.busy) return
        val token = inputState.value.token
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = join(token)) {
                is AppResult.Success -> {
                    inputState.update { it.copy(token = "") }
                    mutableState.update { it.copy(token = "") }
                    refresh()
                }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }
}
