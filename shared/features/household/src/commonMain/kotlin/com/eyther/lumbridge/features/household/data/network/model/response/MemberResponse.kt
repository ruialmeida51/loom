package com.eyther.lumbridge.features.household.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemberResponse(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val name: String,
    @SerialName("role") val role: String,
    @SerialName("active") val active: Boolean = true,
    @SerialName("version") val version: Long = 1,
    @SerialName("avatar_path") val avatarPath: String? = null,
)
