package com.eyther.lumbridge.features.shopping.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ShoppingItemResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("quantity") val quantity: String,
    @SerialName("category") val category: String,
    @SerialName("checked") val checked: Boolean,
    @SerialName("list_id") val listId: String,
    @SerialName("version") val version: Long,
    @SerialName("removed") val removed: Boolean,
    @SerialName("favorite") val favorite: Boolean,
)
