package com.eyther.lumbridge.features.expenses.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemberAccountPositionResponse(
    @SerialName("account_id") val accountId: String,
    @SerialName("account_name") val accountName: String,
    @SerialName("is_default") val isDefault: Boolean,
    @SerialName("currency") val currency: String,
    @SerialName("balance_minor") val balanceMinor: Long,
)
