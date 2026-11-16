package com.eyther.lumbridge.features.household.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.InvitationCodeDomain
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.JoinHouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.JoinHouseholdState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class JoinHouseholdScreenViewModel(
    private val joinHouseholdUseCase: JoinHouseholdUseCase,
) : JoinHouseholdScreenViewModelContract() {

    private data class InnerInputState(val token: String = "")

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(JoinHouseholdState()).observeWith(viewModelScope) { state ->
            inputState.updateState(state) { input -> copy(token = input.token) }
        }

    override val state = mutableState.asStateFlow()

    override fun onInvitationCodeChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(
                token =
                    if (value.trim().length == 64 && InvitationCodeDomain.isComplete(value.trim()))
                        value.trim()
                    else value.trim().uppercase().take(InvitationCodeDomain.LENGTH),
            )
        }
    }

    override fun onSubmit() {
        if (state.value.busy) return
        val token = inputState.value.token
        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = joinHouseholdUseCase(token = token)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(busy = false, redirectTo = RedirectTo.Households)
                    is AppResult.Failure ->
                        oldState.copy(busy = false, error = result.error.toHouseholdTextResource())
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
