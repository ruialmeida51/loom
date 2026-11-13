package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RecurringExpenseResponse(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("paid_by") val paidBy: String,
    @SerialName("category") val category: String,
    @SerialName("next_on") val nextOn: String,
    @SerialName("anchor_day") val anchorDay: Int,
    @SerialName("active") val active: Boolean,
    @SerialName("cancelled_at") val cancelledAt: String?,
    @SerialName("created_by") val createdBy: String,
    @SerialName("version") val version: Long,
    @SerialName("shares") val shares: Map<String, Long>,
)
