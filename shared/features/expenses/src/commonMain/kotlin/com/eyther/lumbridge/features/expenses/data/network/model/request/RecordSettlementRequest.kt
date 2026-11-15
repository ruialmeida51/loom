package com.eyther.lumbridge.features.expenses.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RecordSettlementRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_account_id") val accountId: String,
    @SerialName("p_id") val id: String,
    @SerialName("p_from") val from: String,
    @SerialName("p_to") val to: String,
    @SerialName("p_amount_minor") val amountMinor: Long,
    @SerialName("p_occurred_on") val occurredOn: String,
)
