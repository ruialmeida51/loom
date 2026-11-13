package com.eyther.lumbridge.features.shopping.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ShoppingItemRequest(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("quantity") val quantity: String,
    @SerialName("category") val category: String,
    @SerialName("list_id") val listId: String,
)
