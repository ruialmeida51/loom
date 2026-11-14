package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.authentication.domain.model.EmailCodePurposeDomain
import kotlinx.coroutines.flow.StateFlow

abstract class EmailVerificationScreenViewModelContract : LoomViewModel() {

    /** Navigation carries the address and request status, never a password or verification code. */
    data class Params(
        val email: String,
        val codeRequested: Boolean,
        val purpose: EmailCodePurposeDomain = EmailCodePurposeDomain.REGISTRATION,
    )

    abstract val state: StateFlow<EmailVerificationState>

    abstract fun onCodeChanged(value: String)

    abstract fun onVerify()

    abstract fun onResendCode()

    abstract fun onDismissError()
}
