package com.eyther.lumbridge.features.household.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HouseholdScreenViewModel(
    private val getHouseholdsUseCase: GetHouseholdsUseCase,
    private val createHouseholdUseCase: CreateHouseholdUseCase,
    private val joinHouseholdUseCase: JoinHouseholdUseCase,
) : HouseholdScreenViewModelContract() {

    private data class InnerInputState(
        val name: String = "",
        val token: String = "",
        val currency: String = "EUR",
    )

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState = MutableStateFlow(HouseholdState())
        .observeWith(viewModelScope) { state ->
            inputState.updateState(state) { input ->
                copy(
                    name = input.name,
                    token = input.token,
                    currency = input.currency,
                )
            }
        }
        .launchWith(viewModelScope) { state ->
            state.loadHouseholds()
        }

    override val state = mutableState.asStateFlow()

    override fun onNameChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(name = value)
        }
    }

    override fun onInvitationCodeChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(token = value)
        }
    }

    override fun onCurrencyChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(currency = value)
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }

    override fun onRefresh() {
        launch {
            mutableState.loadHouseholds()
        }
    }

    override fun onCreateHousehold() {
        if (state.value.busy) return
        val input = inputState.value

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            when (val result = createHouseholdUseCase(name = input.name, currency = input.currency)) {
                is AppResult.Success -> {
                    inputState.update { oldState ->
                        oldState.copy(name = "")
                    }
                    mutableState.loadHouseholds()
                }

                is AppResult.Failure -> mutableState.update { oldState ->
                    oldState.copy(
                        busy = false,
                        error = result.error.toHouseholdTextResource(),
                    )
                }
            }
        }
    }

    override fun onJoinHousehold() {
        if (state.value.busy) return
        val token = inputState.value.token

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            when (val result = joinHouseholdUseCase(token)) {
                is AppResult.Success -> {
                    inputState.update { oldState ->
                        oldState.copy(token = "")
                    }
                    mutableState.loadHouseholds()
                }

                is AppResult.Failure -> mutableState.update { oldState ->
                    oldState.copy(
                        busy = false,
                        error = result.error.toHouseholdTextResource(),
                    )
                }
            }
        }
    }

    private suspend fun MutableStateFlow<HouseholdState>.loadHouseholds() {
        val result = getHouseholdsUseCase()

        update { oldState ->
            when (result) {
                is AppResult.Success -> oldState.copy(
                    items = result.value,
                    busy = false,
                    error = null,
                )

                is AppResult.Failure -> oldState.copy(
                    busy = false,
                    error = result.error.toHouseholdTextResource(),
                )
            }
        }
    }
}
