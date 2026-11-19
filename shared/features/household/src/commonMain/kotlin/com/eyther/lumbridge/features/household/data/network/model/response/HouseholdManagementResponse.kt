package com.eyther.lumbridge.features.household.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class HouseholdManagementResponse(
    @SerialName("members") val members: List<MemberResponse>,
    @SerialName("invitation") val invitation: HouseholdInvitationResponse? = null,
)
