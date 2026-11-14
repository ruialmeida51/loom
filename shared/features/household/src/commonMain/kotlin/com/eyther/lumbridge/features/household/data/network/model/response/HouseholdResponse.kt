package com.eyther.lumbridge.features.household.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HouseholdResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("currency") val currency: String,
    @SerialName("time_zone") val timeZone: String,
)
