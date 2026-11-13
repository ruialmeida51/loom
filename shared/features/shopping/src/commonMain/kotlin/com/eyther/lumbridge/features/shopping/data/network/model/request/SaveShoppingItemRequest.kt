package com.eyther.lumbridge.features.shopping.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SaveShoppingItemRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_id") val id: String,
    @SerialName("p_name") val name: String,
    @SerialName("p_quantity") val quantity: String,
    @SerialName("p_category") val category: String,
    @SerialName("p_checked") val checked: Boolean,
    @SerialName("p_list_id") val listId: String,
    @SerialName("p_version") val version: Long,
    @SerialName("p_removed") val removed: Boolean,
    @SerialName("p_favorite") val favorite: Boolean,
)
