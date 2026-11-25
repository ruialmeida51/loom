package com.eyther.lumbridge.features.household.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HouseholdFeatureResponse(
    @SerialName("feature") val feature: String,
    @SerialName("enabled") val enabled: Boolean = true,
)
