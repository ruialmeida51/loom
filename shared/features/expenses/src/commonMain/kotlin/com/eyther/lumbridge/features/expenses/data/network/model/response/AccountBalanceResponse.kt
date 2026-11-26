package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One person's balance in an account, from the `account_balances` view. */
@Serializable
internal data class AccountBalanceResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("balance_minor") val balanceMinor: Long,
)
