package com.eyther.lumbridge.features.household.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class JoinHouseholdRequest(
    @SerialName("p_token") val token: String,
)
