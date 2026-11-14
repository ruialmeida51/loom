package com.eyther.lumbridge.features.settings.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PushDeviceRequest(
    @SerialName("p_device_id") val deviceId: String,
)
