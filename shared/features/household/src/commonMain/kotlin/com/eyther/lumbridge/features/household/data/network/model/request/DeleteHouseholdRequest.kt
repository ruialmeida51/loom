package com.eyther.lumbridge.features.household.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DeleteHouseholdRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_name") val name: String,
    @SerialName("p_request_id") val requestId: String,
)
