package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExpenseResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("paid_by") val paidBy: String,
    @SerialName("category") val category: String,
    @SerialName("occurred_on") val occurredOn: String,
    @SerialName("shares") val shares: Map<String, Long>,
    @SerialName("recurring") val recurring: Boolean,
)
