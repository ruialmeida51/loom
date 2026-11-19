package com.eyther.lumbridge.features.household.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ChangeHouseholdMemberRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_member_id") val memberId: String,
    @SerialName("p_action") val action: String,
    @SerialName("p_version") val version: Long,
    @SerialName("p_request_id") val requestId: String,
)
