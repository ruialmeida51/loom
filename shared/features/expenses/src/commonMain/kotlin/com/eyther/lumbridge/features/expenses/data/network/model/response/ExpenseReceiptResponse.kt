package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExpenseReceiptResponse(
    @SerialName("expense_id") val expenseId: String,
    @SerialName("account_id") val accountId: String,
    @SerialName("object_path") val objectPath: String?,
    @SerialName("version") val version: Long,
)
