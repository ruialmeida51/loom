package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExpenseAccountResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("currency") val currency: String,
    @SerialName("member_ids") val memberIds: List<String>,
    @SerialName("is_default") val isDefault: Boolean,
    @SerialName("archived") val archived: Boolean,
    @SerialName("version") val version: Long,
    @SerialName("created_by") val createdBy: String,
)
