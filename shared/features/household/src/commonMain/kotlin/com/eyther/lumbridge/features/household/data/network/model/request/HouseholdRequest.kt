package com.eyther.lumbridge.features.household.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HouseholdRequest(
    @SerialName("p_household_id") val householdId: String,
)
