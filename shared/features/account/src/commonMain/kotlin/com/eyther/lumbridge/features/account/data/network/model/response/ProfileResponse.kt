package com.eyther.lumbridge.features.account.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProfileResponse(
    @SerialName("id") val id: String,
    @SerialName("display_name") val displayName: String,
)
