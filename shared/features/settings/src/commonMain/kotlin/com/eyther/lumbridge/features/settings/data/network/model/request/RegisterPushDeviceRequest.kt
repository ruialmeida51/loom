package com.eyther.lumbridge.features.settings.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RegisterPushDeviceRequest(
    @SerialName("p_device_id") val deviceId: String,
    @SerialName("p_token") val token: String,
    @SerialName("p_platform") val platform: String,
)
