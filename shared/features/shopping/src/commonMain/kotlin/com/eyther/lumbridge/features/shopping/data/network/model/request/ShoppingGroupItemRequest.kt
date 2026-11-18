package com.eyther.lumbridge.features.shopping.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ShoppingGroupItemRequest(
    @SerialName("id") val id: String,
    @SerialName("list_id") val listId: String,
    @SerialName("name") val name: String,
    @SerialName("quantity") val quantity: String,
    @SerialName("category") val category: String,
    @SerialName("checked") val checked: Boolean,
    @SerialName("favorite") val favorite: Boolean,
    @SerialName("removed") val removed: Boolean,
    @SerialName("version") val version: Long,
    @SerialName("parent_id") val parentId: String?,
)
