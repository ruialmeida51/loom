package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SettlementResponse(
    @SerialName("account_id") val accountId: String,
    @SerialName("id") val id: String,
    @SerialName("from_user") val from: String,
    @SerialName("to_user") val to: String,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("occurred_on") val occurredOn: String,
    @SerialName("created_by") val createdBy: String = "",
    @SerialName("version") val version: Long = 1,
)
