package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
internal data class ExpenseResponse(
    @SerialName("account_id") val accountId: String,
    @SerialName("version") val version: Long,
    @SerialName("created_by") val createdBy: String,
    @SerialName("split_method") val splitMethod: String,
    @SerialName("split_values") val splitValues: Map<String, Long>,
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("amount_minor") val amountMinor: Long,
    @SerialName("paid_by") val paidBy: String,
    @SerialName("category") val category: String,
    @SerialName("occurred_on") val occurredOn: String,
    @SerialName("shares") val shares: Map<String, Long>,
    @SerialName("recurring") val recurring: Boolean,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("original_currency") val originalCurrency: String? = null,
    @SerialName("original_amount_minor") val originalAmountMinor: Long? = null,
    // A numeric column: kept as its literal text so a rate like 0.0004 never becomes "4.0E-4".
    @SerialName("exchange_rate") val exchangeRate: JsonPrimitive? = null,
)
