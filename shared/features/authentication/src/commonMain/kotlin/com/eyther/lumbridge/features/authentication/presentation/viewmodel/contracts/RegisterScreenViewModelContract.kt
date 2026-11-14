package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class RegisterScreenViewModelContract : LoomViewModel() {

    @Stable
    sealed interface RedirectTo {
        @Stable
        data class VerifyEmail(val email: String) : RedirectTo
    }

    abstract val state: StateFlow<RegisterState>

    abstract fun onEmailChanged(value: String)

    abstract fun onPasswordChanged(value: String)

    abstract fun onTermsAcceptedChanged(accepted: Boolean)

    abstract fun onSubmit()

    abstract fun onDismissError()

    abstract fun onRedirectHandled()
}
