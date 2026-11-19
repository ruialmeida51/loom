package com.eyther.lumbridge.features.shopping.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class InsertShoppingItemAfterRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_item") val item: ShoppingItemRequest,
    @SerialName("p_after_id") val afterId: String,
)
