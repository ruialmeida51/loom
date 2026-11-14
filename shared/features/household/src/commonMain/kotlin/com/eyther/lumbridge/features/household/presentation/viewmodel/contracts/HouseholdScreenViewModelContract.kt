package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class HouseholdScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<HouseholdState>

    abstract fun onDismissError()

    abstract fun onRefresh()

}
