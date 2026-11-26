package com.eyther.lumbridge.features.expenses.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SaveExpenseRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_account_id") val accountId: String,
    @SerialName("p_version") val version: Long,
    @SerialName("p_split_method") val splitMethod: String,
    @SerialName("p_split_values") val splitValues: Map<String, Long>,
    @SerialName("p_id") val id: String,
    @SerialName("p_title") val title: String,
    @SerialName("p_amount_minor") val amountMinor: Long,
    @SerialName("p_paid_by") val paidBy: String,
    @SerialName("p_category") val category: String,
    @SerialName("p_occurred_on") val occurredOn: String,
    @SerialName("p_shares") val shares: Map<String, Long>,
    @SerialName("p_recurring") val recurring: Boolean,
    @SerialName("p_original_currency") val originalCurrency: String? = null,
    @SerialName("p_original_amount_minor") val originalAmountMinor: Long? = null,
    @SerialName("p_exchange_rate") val exchangeRate: String? = null,
)
