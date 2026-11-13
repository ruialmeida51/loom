package com.eyther.lumbridge.features.shopping.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ShoppingListResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("archived") val archived: Boolean,
    @SerialName("version") val version: Long,
)
