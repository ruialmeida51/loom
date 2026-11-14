package com.eyther.lumbridge.features.expenses.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ControlRecurringExpenseRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_id") val id: String,
    @SerialName("p_action") val action: String,
    @SerialName("p_version") val version: Long,
)
