package com.eyther.lumbridge.features.insights.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SaveStatementRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_month") val month: String,
)
