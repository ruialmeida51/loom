package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class HouseholdDetailsScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<HouseholdDetailsState>

    abstract fun onRefresh()

    abstract fun onCreateInvitation()

    abstract fun onRevokeInvitation()

    abstract fun onRevokeInvitationCancelled()

    abstract fun onRevokeInvitationConfirmed()

    abstract fun onCloseInvitation()

    abstract fun onDismissError()
}
