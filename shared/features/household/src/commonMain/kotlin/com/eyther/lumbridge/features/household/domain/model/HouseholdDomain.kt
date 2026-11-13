package com.eyther.lumbridge.features.household.domain.model

import com.eyther.lumbridge.core.domain.*

data class HouseholdDomain(
    val id: String,
    val name: String,
    val currency: String,
    val timeZone: String,
)
