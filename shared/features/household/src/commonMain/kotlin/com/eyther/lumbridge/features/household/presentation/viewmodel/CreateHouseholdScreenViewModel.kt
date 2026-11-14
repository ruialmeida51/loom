package com.eyther.lumbridge.features.household.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.CreateHouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.CreateHouseholdState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CreateHouseholdScreenViewModel(
    private val createHouseholdUseCase: CreateHouseholdUseCase,
) : CreateHouseholdScreenViewModelContract() {

    private data class InnerInputState(val name: String = "", val currency: String = "EUR")

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState = MutableStateFlow(CreateHouseholdState())
        .observeWith(viewModelScope) { state ->
            inputState.updateState(state) { input -> copy(name = input.name, currency = input.currency) }
        }

    override val state = mutableState.asStateFlow()

    override fun onNameChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(name = value)
        }
    }

    override fun onCurrencyChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(currency = value)
        }
    }

    override fun onSubmit() {
        if (state.value.busy) return
        val input = inputState.value
        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = createHouseholdUseCase(name = input.name, currency = input.currency)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        busy = false,
                        redirectTo = RedirectTo.OpenHousehold(household = result.value),
                    )
                    is AppResult.Failure -> oldState.copy(busy = false, error = result.error.toHouseholdTextResource())
                }
            }
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }

    override fun onRedirectHandled() {
        mutableState.update { oldState ->
            oldState.copy(redirectTo = null)
        }
    }
}
