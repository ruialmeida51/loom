package com.eyther.lumbridge.features.authentication.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.authentication.domain.usecase.RegisterUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInUseCase
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.registration_confirmation_sent
import com.eyther.lumbridge.features.authentication.presentation.mapper.toAuthenticationTextResource
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthenticationScreenViewModel(
    private val signInUseCase: SignInUseCase,
    private val registerUseCase: RegisterUseCase,
) : AuthenticationScreenViewModelContract() {

    private data class InnerInputState(
        val email: String = "",
        val password: String = "",
        val registering: Boolean = false,
    )

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState = MutableStateFlow(AuthenticationState())
        .observeWith(viewModelScope) { state ->
            inputState.updateState(state) { input ->
                copy(
                    email = input.email,
                    password = input.password,
                    registering = input.registering,
                )
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

    override fun onToggleRegistration() {
        inputState.update { oldState ->
            oldState.copy(registering = !oldState.registering)
        }
        mutableState.update { oldState ->
            oldState.copy(error = null, message = null)
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }

    override fun onSubmit() {
        if (state.value.busy) return
        val input = inputState.value

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null, message = null)
        }

        launch {
            val result = if (input.registering) {
                registerUseCase(email = input.email, password = input.password)
            } else {
                signInUseCase(email = input.email, password = input.password)
            }

            if (result is AppResult.Success) {
                inputState.update { oldState ->
                    oldState.copy(password = "")
                }
            }

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        error = result.error.toAuthenticationTextResource(),
                    )

                    is AppResult.Success -> oldState.copy(
                        busy = false,
                        password = "",
                        message = if (input.registering) {
                            TextResource.Resource(Res.string.registration_confirmation_sent)
                        } else {
                            null
                        },
                    )
                }
            }
        }
    }
}
