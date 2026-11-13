package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class AuthenticationScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<AuthenticationState>

    abstract fun onEmailChanged(value: String)

    abstract fun onPasswordChanged(value: String)

    abstract fun onToggleRegistration()

    abstract fun onSubmit()

    abstract fun onDismissError()
}
