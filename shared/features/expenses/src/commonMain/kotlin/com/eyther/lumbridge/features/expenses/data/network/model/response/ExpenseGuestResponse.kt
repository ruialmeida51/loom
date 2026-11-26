package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExpenseGuestResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("removed") val removed: Boolean = false,
    @SerialName("version") val version: Long = 1,
)
