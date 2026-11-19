package com.eyther.lumbridge.features.household.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HouseholdInvitationResponse(
    @SerialName("id") val id: String,
    @SerialName("version") val version: Long,
    @SerialName("code") val code: String? = null,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("reusable") val reusable: Boolean,
)
