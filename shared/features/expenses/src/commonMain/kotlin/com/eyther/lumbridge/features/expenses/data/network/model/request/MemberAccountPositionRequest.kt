package com.eyther.lumbridge.features.expenses.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MemberAccountPositionRequest(
    @SerialName("p_household_id") val householdId: String,
    @SerialName("p_member_id") val memberId: String,
)
