package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlinx.coroutines.flow.*

data class HouseholdState(
    val items: List<HouseholdDomain> = emptyList(),
    val name: String = "",
    val token: String = "",
    val currency: String = "EUR",
    val busy: Boolean = false,
    val error: String? = null,
)

abstract class HouseholdScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<HouseholdState>

    abstract fun name(value: String)

    abstract fun token(value: String)

    abstract fun currency(value: String)

    abstract fun dismiss()

    abstract fun refresh()

    abstract fun create()

    abstract fun join()
}
