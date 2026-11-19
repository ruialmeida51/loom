package com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlinx.coroutines.flow.StateFlow

abstract class SessionScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<SessionState>

    abstract fun onHouseholdSelected(household: HouseholdDomain)

    abstract fun onHouseholdExited(householdId: String, explicit: Boolean)

    abstract fun onRetryHouseholdRestore()

    abstract fun onChooseHousehold()

    abstract fun onDismissSelectionError()

    abstract fun onSignOutDismissed()

    abstract fun onSignOut()
}
