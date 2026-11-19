package com.eyther.lumbridge.features.household.data.network.mapper

import com.eyther.lumbridge.features.household.data.network.model.response.HouseholdInvitationResponse
import com.eyther.lumbridge.features.household.data.network.model.response.HouseholdManagementResponse
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdManagementDomain

internal fun HouseholdInvitationResponse.toDomain(): HouseholdInvitationDomain =
    HouseholdInvitationDomain(
        id = id,
        version = version,
        code = code,
        expiresAt = expiresAt,
        reusable = reusable,
    )

internal fun HouseholdManagementResponse.toDomain(): HouseholdManagementDomain =
    HouseholdManagementDomain(
        members = members.map { it.toDomain() },
        invitation = invitation?.toDomain(),
    )
