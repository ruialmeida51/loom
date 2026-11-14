package com.eyther.lumbridge.features.shopping.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SaveShoppingListRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_id") val id: String,
    @SerialName("p_name") val name: String,
    @SerialName("p_archived") val archived: Boolean,
    @SerialName("p_version") val version: Long,
)
