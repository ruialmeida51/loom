package com.eyther.lumbridge.features.household.data.network.mapper

import com.eyther.lumbridge.features.household.data.network.model.response.HouseholdResponse
import com.eyther.lumbridge.features.household.data.network.model.response.MemberResponse
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain

internal fun HouseholdResponse.toDomain(): HouseholdDomain = HouseholdDomain(
    id = id,
    name = name,
    currency = currency,
    timeZone = timeZone,
)

internal fun MemberResponse.toDomain(): MemberDomain = MemberDomain(
    id = userId,
    name = name,
    role = role,
)
