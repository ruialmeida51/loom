package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** What one person paid in an account, from the `account_spending` view. */
@Serializable
internal data class AccountSpendingResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("expense_count") val expenseCount: Long,
    @SerialName("paid_minor") val paidMinor: Long,
)
