package com.eyther.lumbridge.features.account.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentHandlesResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("mbway_phone") val mbwayPhone: String? = null,
    @SerialName("revolut_username") val revolutUsername: String? = null,
    @SerialName("paypal_username") val paypalUsername: String? = null,
)
