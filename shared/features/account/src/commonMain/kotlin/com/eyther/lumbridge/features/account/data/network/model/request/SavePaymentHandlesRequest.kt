package com.eyther.lumbridge.features.account.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SavePaymentHandlesRequest(
    @SerialName("user_id") val userId: String,
    @SerialName("mbway_phone") val mbwayPhone: String?,
    @SerialName("revolut_username") val revolutUsername: String?,
    @SerialName("paypal_username") val paypalUsername: String?,
)
