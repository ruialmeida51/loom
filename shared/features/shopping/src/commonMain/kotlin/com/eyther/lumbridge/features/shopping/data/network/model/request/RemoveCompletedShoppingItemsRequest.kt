package com.eyther.lumbridge.features.shopping.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoveCompletedShoppingItemsRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_list_id") val listId: String,
    @SerialName("p_versions") val versions: Map<String, Long>,
)
