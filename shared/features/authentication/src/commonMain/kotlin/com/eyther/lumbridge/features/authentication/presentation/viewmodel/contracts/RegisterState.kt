package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource

data class RegisterState(
    val email: String = "",
    val password: String = "",
    val acceptedTerms: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val redirectTo: RegisterScreenViewModelContract.RedirectTo? = null,
) {

    val canSubmit: Boolean = email.isNotBlank() && password.isNotBlank() && acceptedTerms
}
