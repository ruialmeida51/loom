package com.eyther.lumbridge.features.authentication.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.usecase.RegisterUseCase
import com.eyther.lumbridge.features.authentication.presentation.mapper.toAuthenticationTextResource
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.RegisterScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.RegisterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegisterScreenViewModel(
    private val registerUseCase: RegisterUseCase,
) : RegisterScreenViewModelContract() {

    private data class InnerInputState(
        val email: String = "",
        val password: String = "",
        val acceptedTerms: Boolean = false,
    )

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState = MutableStateFlow(RegisterState())
        .observeWith(viewModelScope) { state ->
            inputState.updateState(state) { input ->
                copy(email = input.email, password = input.password, acceptedTerms = input.acceptedTerms)
            }
        }

    override val state = mutableState.asStateFlow()

    override fun onEmailChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(email = value)
        }
    }

    override fun onPasswordChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(password = value)
        }
    }

    override fun onTermsAcceptedChanged(accepted: Boolean) {
        inputState.update { oldState ->
            oldState.copy(acceptedTerms = accepted)
        }
    }

    override fun onSubmit() {
        if (state.value.busy) return
        val input = inputState.value
        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = registerUseCase(
                email = input.email,
                password = input.password,
                acceptedTerms = input.acceptedTerms,
            )
            if (result is AppResult.Success) {
                inputState.update { oldState ->
                    oldState.copy(password = "")
                }
            }

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        busy = false,
                        password = "",
                        redirectTo = RedirectTo.VerifyEmail(email = input.email.trim()),
                    )
                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        error = result.error.toAuthenticationTextResource(),
                    )
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
