package com.eyther.lumbridge.features.expenses.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SaveExpenseReceiptRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_account_id") val accountId: String,
    @SerialName("p_expense_id") val expenseId: String,
    @SerialName("p_object_path") val objectPath: String?,
    @SerialName("p_version") val version: Long,
)
