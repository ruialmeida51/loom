package com.eyther.lumbridge.features.household.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain

data class HouseholdState(
    val items: List<HouseholdDomain> = emptyList(),
    val busy: Boolean = false,
    val error: TextResource? = null,
)
