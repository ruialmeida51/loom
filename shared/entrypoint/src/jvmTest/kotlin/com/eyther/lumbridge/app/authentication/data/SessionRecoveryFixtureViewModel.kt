package com.eyther.lumbridge.app.authentication.data

import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionState
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class SessionRecoveryFixtureViewModel : SessionScreenViewModelContract() {
    override val state = MutableStateFlow(SessionState())
    var retries = 0
        private set

    override fun onResume() = Unit

    override fun onPause() = Unit

    override fun onRetrySessionRecovery() {
        retries++
        state.update { oldState -> oldState.copy(recoveringSession = true) }
    }

    override fun onHouseholdSelected(household: HouseholdDomain): Unit = error("Not used")

    override fun onHouseholdExited(householdId: String, explicit: Boolean): Unit = error("Not used")

    override fun onRetryHouseholdRestore(): Unit = error("Not used")

    override fun onChooseHousehold(): Unit = error("Not used")

    override fun onDismissSelectionError(): Unit = error("Not used")

    override fun onSignOutDismissed(): Unit = error("Not used")

    override fun onSignOut(): Unit = error("Not used")
}
