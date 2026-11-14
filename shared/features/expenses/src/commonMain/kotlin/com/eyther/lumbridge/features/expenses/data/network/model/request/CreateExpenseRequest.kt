package com.eyther.lumbridge.features.expenses.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateExpenseRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_id") val id: String,
    @SerialName("p_title") val title: String,
    @SerialName("p_amount_minor") val amountMinor: Long,
    @SerialName("p_paid_by") val paidBy: String,
    @SerialName("p_category") val category: String,
    @SerialName("p_occurred_on") val occurredOn: String,
    @SerialName("p_shares") val shares: Map<String, Long>,
    @SerialName("p_recurring") val recurring: Boolean,
)
