package com.eyther.lumbridge.features.household.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateHouseholdRequest(
    @SerialName("p_name") val name: String,
    @SerialName("p_currency") val currency: String,
    @SerialName("p_time_zone") val timeZone: String,
)
