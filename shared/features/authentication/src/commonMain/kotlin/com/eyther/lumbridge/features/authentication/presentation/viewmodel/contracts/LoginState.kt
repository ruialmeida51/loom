package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource

data class LoginState(
    val email: String = "",
    val password: String = "",
    val signingIn: Boolean = false,
    val requestingCode: Boolean = false,
    val startingGoogle: Boolean = false,
    val error: TextResource? = null,
    val googleError: TextResource? = null,
    val redirectTo: LoginScreenViewModelContract.RedirectTo? = null,
) {

    val busy: Boolean = signingIn || requestingCode || startingGoogle

    val displayError: TextResource? = error ?: googleError

    val canSubmit: Boolean = email.isNotBlank() && password.isNotBlank()

    val canRequestCode: Boolean = email.isNotBlank()
}
