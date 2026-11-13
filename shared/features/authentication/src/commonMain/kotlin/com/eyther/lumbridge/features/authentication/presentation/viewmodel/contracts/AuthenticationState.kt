package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource

data class AuthenticationState(
    val email: String = "",
    val password: String = "",
    val registering: Boolean = false,
    val busy: Boolean = false,
    val message: TextResource? = null,
    val error: TextResource? = null,
)
