package com.eyther.lumbridge.features.authentication.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.authentication.domain.usecase.RegisterUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInUseCase
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationState
import kotlinx.coroutines.flow.*

class AuthenticationScreenViewModel(
    private val signIn: SignInUseCase,
    private val register: RegisterUseCase,
) : AuthenticationScreenViewModelContract() {
    private data class InnerInputState(
        val email: String = "",
        val password: String = "",
        val registering: Boolean = false,
    )

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(AuthenticationState()).observeWith(viewModelScope) { state ->
            inputState.updateState(state) {
                copy(email = it.email, password = it.password, registering = it.registering)
            }
        }
    override val state = mutableState.asStateFlow()

    override fun email(value: String) {
        inputState.update { it.copy(email = value) }
    }

    override fun password(value: String) {
        inputState.update { it.copy(password = value) }
    }

    override fun toggleRegister() {
        inputState.update { it.copy(registering = !it.registering) }
        mutableState.update { it.copy(error = null, message = null) }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }

    override fun submit() {
        if (mutableState.value.busy) return
        val input = inputState.value
        mutableState.update { it.copy(busy = true, error = null, message = null) }
        launch {
            val result =
                if (input.registering) register(input.email, input.password)
                else signIn(input.email, input.password)
            if (result is AppResult.Success) inputState.update { it.copy(password = "") }
            mutableState.update { old ->
                when (result) {
                    is AppResult.Failure -> old.copy(busy = false, error = result.message)
                    is AppResult.Success ->
                        old.copy(
                            busy = false,
                            password = "",
                            message =
                                if (input.registering)
                                    "authentication.message_check_your_email_to_confirm_your_account_then_sign"
                                else null,
                        )
                }
            }
        }
    }
}
