package com.eyther.lumbridge.features.authentication.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import com.eyther.lumbridge.features.authentication.domain.model.EmailCodePurposeDomain
import com.eyther.lumbridge.features.authentication.domain.usecase.GetGoogleSignInResultsStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RequestEmailCodeUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInWithGoogleUseCase
import com.eyther.lumbridge.features.authentication.presentation.mapper.toAuthenticationTextResource
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.LoginScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.LoginState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginScreenViewModel(
    private val signInUseCase: SignInUseCase,
    private val requestEmailCodeUseCase: RequestEmailCodeUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val getGoogleSignInResultsStreamUseCase: GetGoogleSignInResultsStreamUseCase,
) : LoginScreenViewModelContract() {

    private data class InnerInputState(
        val email: String = "",
        val password: String = "",
    )

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState = MutableStateFlow(LoginState())
        .observeWith(viewModelScope) { state ->
            inputState.updateState(state) { input ->
                copy(email = input.email, password = input.password)
            }
        }
        .observeWith(viewModelScope) { state ->
            getGoogleSignInResultsStreamUseCase().updateState(state) { result ->
                when (result) {
                    is AppResult.Success -> copy(googleError = null)
                    is AppResult.Failure -> copy(googleError = result.error.toAuthenticationTextResource())
                }
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

    override fun onCreateAccount() {
        if (state.value.busy) return
        clearPassword()
        mutableState.update { oldState ->
            oldState.copy(error = null, googleError = null, redirectTo = RedirectTo.Register)
        }
    }

    override fun onRequestEmailCode() {
        if (state.value.busy) return
        val email = inputState.value.email.trim()
        mutableState.update { oldState ->
            oldState.copy(requestingCode = true, error = null, googleError = null)
        }

        launch {
            val result = requestEmailCodeUseCase(email = email, purpose = EmailCodePurposeDomain.SIGN_IN)
            if (result is AppResult.Success) clearPassword()
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        requestingCode = false,
                        redirectTo = RedirectTo.VerifyEmail(
                            email = email,
                            codeRequested = true,
                            purpose = EmailCodePurposeDomain.SIGN_IN,
                        ),
                    )
                    is AppResult.Failure -> oldState.copy(
                        requestingCode = false,
                        error = result.error.toAuthenticationTextResource(),
                    )
                }
            }
        }
    }

    override fun onSubmit() {
        if (state.value.busy) return
        val input = inputState.value
        mutableState.update { oldState ->
            oldState.copy(signingIn = true, error = null, googleError = null)
        }

        launch {
            val result = signInUseCase(email = input.email, password = input.password)
            if (result is AppResult.Success ||
                result is AppResult.Failure && result.error is AuthenticationError.EmailNotConfirmed
            ) {
                clearPassword()
            }

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(signingIn = false)
                    is AppResult.Failure -> {
                        if (result.error is AuthenticationError.EmailNotConfirmed) {
                            oldState.copy(
                                signingIn = false,
                                redirectTo = RedirectTo.VerifyEmail(
                                    email = input.email.trim(),
                                    codeRequested = false,
                                    purpose = EmailCodePurposeDomain.REGISTRATION,
                                ),
                            )
                        } else {
                            oldState.copy(signingIn = false, error = result.error.toAuthenticationTextResource())
                        }
                    }
                }
            }
        }
    }

    override fun onGoogleSignIn() {
        if (state.value.busy) return
        clearPassword()
        mutableState.update { oldState ->
            oldState.copy(startingGoogle = true, error = null, googleError = null)
        }

        launch {
            val result = signInWithGoogleUseCase()
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(startingGoogle = false)
                    is AppResult.Failure -> oldState.copy(
                        startingGoogle = false,
                        googleError = result.error.toAuthenticationTextResource(),
                    )
                }
            }
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, googleError = null)
        }
    }

    override fun onRedirectHandled() {
        mutableState.update { oldState ->
            oldState.copy(redirectTo = null)
        }
    }

    private fun clearPassword() {
        inputState.update { oldState ->
            oldState.copy(password = "")
        }
        mutableState.update { oldState ->
            oldState.copy(password = "")
        }
    }
}
