package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.authentication.domain.model.EmailCodePurposeDomain
import kotlinx.coroutines.flow.StateFlow

abstract class LoginScreenViewModelContract : LoomViewModel() {

    @Stable
    sealed interface RedirectTo {
        @Stable
        data object Register : RedirectTo

        @Stable
        data class VerifyEmail(
            val email: String,
            val codeRequested: Boolean,
            val purpose: EmailCodePurposeDomain,
        ) : RedirectTo
    }

    abstract val state: StateFlow<LoginState>

    abstract fun onEmailChanged(value: String)

    abstract fun onPasswordChanged(value: String)

    abstract fun onSubmit()

    abstract fun onGoogleSignIn()

    abstract fun onCreateAccount()

    abstract fun onRequestEmailCode()

    abstract fun onDismissError()

    abstract fun onRedirectHandled()
}
