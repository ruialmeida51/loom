package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class JoinHouseholdScreenViewModelContract : LoomViewModel() {

    @Stable
    sealed interface RedirectTo {
        @Stable
        data object Households : RedirectTo
    }

    abstract val state: StateFlow<JoinHouseholdState>

    abstract fun onInvitationCodeChanged(value: String)

    abstract fun onSubmit()

    abstract fun onDismissError()

    abstract fun onRedirectHandled()
}
