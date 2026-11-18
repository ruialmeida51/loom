package com.eyther.lumbridge.features.shopping.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SaveShoppingGroupRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_items") val items: List<ShoppingGroupItemRequest>,
)
