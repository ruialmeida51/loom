package com.eyther.lumbridge.features.household.data.network.mapper

import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.features.household.data.network.model.response.JoinHouseholdResponse
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError

internal fun JoinHouseholdResponse.requireJoined(): Unit =
    when (status) {
        "joined" -> Unit
        "invalid" -> throw HouseholdError.ExpiredInvitation()
        "rate_limited" -> throw HouseholdError.InvitationRateLimited()
        "already_joined" -> throw HouseholdError.AlreadyJoined()
        "full" -> throw HouseholdError.HouseholdFull()
        else -> throw CommonError.InvalidResponse()
    }
