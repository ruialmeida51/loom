package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class HouseholdScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<HouseholdState>

    abstract fun onNameChanged(value: String)

    abstract fun onInvitationCodeChanged(value: String)

    abstract fun onCurrencyChanged(value: String)

    abstract fun onDismissError()

    abstract fun onRefresh()

    abstract fun onCreateHousehold()

    abstract fun onJoinHousehold()
}
