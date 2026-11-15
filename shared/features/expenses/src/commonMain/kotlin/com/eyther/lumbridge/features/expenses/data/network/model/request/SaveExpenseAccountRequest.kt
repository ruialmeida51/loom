package com.eyther.lumbridge.features.expenses.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SaveExpenseAccountRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_id") val id: String,
    @SerialName("p_name") val name: String,
    @SerialName("p_currency") val currency: String,
    @SerialName("p_member_ids") val memberIds: Set<String>,
    @SerialName("p_archived") val archived: Boolean,
    @SerialName("p_version") val version: Long,
)
