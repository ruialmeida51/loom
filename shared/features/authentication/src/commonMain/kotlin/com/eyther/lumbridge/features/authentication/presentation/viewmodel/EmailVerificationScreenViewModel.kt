package com.eyther.lumbridge.features.authentication.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import com.eyther.lumbridge.features.authentication.domain.usecase.RequestEmailCodeUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.VerifyEmailUseCase
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.verification_code_requested
import com.eyther.lumbridge.features.authentication.presentation.mapper.toAuthenticationTextResource
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.EmailVerificationScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.EmailVerificationState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EmailVerificationScreenViewModel(
    params: Params,
    private val verifyEmailUseCase: VerifyEmailUseCase,
    private val requestEmailCodeUseCase: RequestEmailCodeUseCase,
) : EmailVerificationScreenViewModelContract() {

    private data class InnerInputState(val code: String = "")

    private val purpose = params.purpose

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState = MutableStateFlow(EmailVerificationState(email = params.email))
        .observeWith(viewModelScope) { state ->
            inputState.updateState(state) { input -> copy(code = input.code) }
        }

    override val state = mutableState.asStateFlow()

    private var cooldownJob: Job? = null

    init {
        if (params.codeRequested) startResendCooldown()
    }

    override fun onCodeChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(code = value.filter { it in '0'..'9' }.take(10))
        }
    }

    override fun onVerify() {
        if (state.value.busy) return
        val code = inputState.value.code
        val email = state.value.email
        mutableState.update { oldState ->
            oldState.copy(verifying = true, error = null, message = null)
        }

        launch {
            val result = verifyEmailUseCase(email = email, code = code)
            if (result is AppResult.Success) {
                inputState.update { oldState ->
                    oldState.copy(code = "")
                }
            }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(verifying = false, code = "")
                    is AppResult.Failure -> oldState.copy(
                        verifying = false,
                        error = result.error.toAuthenticationTextResource(),
                    )
                }
            }
        }
    }

    override fun onResendCode() {
        if (!state.value.canResend) return
        val email = state.value.email
        mutableState.update { oldState ->
            oldState.copy(resending = true, error = null, message = null)
        }

        launch {
            val result = requestEmailCodeUseCase(email = email, purpose = purpose)
            if (result is AppResult.Success ||
                result is AppResult.Failure && result.error is AuthenticationError.TooManyAttempts
            ) {
                startResendCooldown()
            }
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        resending = false,
                        message = TextResource.Resource(Res.string.verification_code_requested),
                    )
                    is AppResult.Failure -> oldState.copy(
                        resending = false,
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

    /** Prevents accidental repeat requests; Supabase remains authoritative for rate limits. */
    private fun startResendCooldown() {
        cooldownJob?.cancel()
        mutableState.update { oldState ->
            oldState.copy(resendInSeconds = 60)
        }
        cooldownJob = launch {
            repeat(60) {
                delay(1000)
                mutableState.update { oldState ->
                    oldState.copy(resendInSeconds = (oldState.resendInSeconds - 1).coerceAtLeast(0))
                }
            }
        }
    }
}
