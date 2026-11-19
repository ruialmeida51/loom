package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import kotlinx.coroutines.flow.StateFlow

abstract class HouseholdDetailsScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<HouseholdDetailsState>

    abstract fun onRefresh()

    abstract fun onCreateInvitation()

    abstract fun onRevokeInvitation()

    abstract fun onMemberAction(memberId: String, action: HouseholdMemberActionDomain)

    abstract fun onLeaveRequested()

    abstract fun onDeleteRequested()

    abstract fun onConfirmationNameChanged(value: String)

    abstract fun onConfirm()

    abstract fun onConfirmationDismissed()

    abstract fun onCopiedInvitation()

    abstract fun onShareUnavailable()

    abstract fun onDismissError()
}
