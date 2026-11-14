package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import androidx.compose.runtime.Stable
import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlinx.coroutines.flow.StateFlow

abstract class CreateHouseholdScreenViewModelContract : LoomViewModel() {

    @Stable
    sealed interface RedirectTo {
        @Stable
        data class OpenHousehold(val household: HouseholdDomain) : RedirectTo
    }

    abstract val state: StateFlow<CreateHouseholdState>

    abstract fun onNameChanged(value: String)

    abstract fun onCurrencyChanged(value: String)

    abstract fun onSubmit()

    abstract fun onDismissError()

    abstract fun onRedirectHandled()
}
