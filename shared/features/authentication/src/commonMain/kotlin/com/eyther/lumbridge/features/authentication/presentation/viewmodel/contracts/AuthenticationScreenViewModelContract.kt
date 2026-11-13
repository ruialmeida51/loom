package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.eyther.lumbridge.core.presentation.*
import kotlinx.coroutines.flow.*

data class AuthenticationState(
    val email: String = "",
    val password: String = "",
    val registering: Boolean = false,
    val busy: Boolean = false,
    val message: String? = null,
    val error: String? = null,
)

abstract class AuthenticationScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<AuthenticationState>

    abstract fun email(value: String)

    abstract fun password(value: String)

    abstract fun toggleRegister()

    abstract fun submit()

    abstract fun demo()

    abstract fun dismiss()
}
