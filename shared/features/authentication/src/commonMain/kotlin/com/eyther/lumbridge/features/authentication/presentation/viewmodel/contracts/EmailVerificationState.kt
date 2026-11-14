package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource

data class EmailVerificationState(
    val email: String,
    val code: String = "",
    val verifying: Boolean = false,
    val resending: Boolean = false,
    val resendInSeconds: Int = 0,
    val error: TextResource? = null,
    val message: TextResource? = null,
) {

    val busy: Boolean = verifying || resending

    val canVerify: Boolean = code.isNotBlank() && !busy

    val canResend: Boolean = resendInSeconds == 0 && !busy
}
