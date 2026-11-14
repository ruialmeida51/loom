package com.eyther.lumbridge.features.household.data.network.mapper

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import io.github.jan.supabase.postgrest.exception.PostgrestRestException

internal fun Exception.toHouseholdError(): AppError {
    if (this !is PostgrestRestException) return toAppError()

    if (code != "P0001") return toAppError()

    return when (error) {
        "Only the household owner can invite members." -> HouseholdError.OwnerRequired(cause = this)
        "This invitation is invalid or has expired." -> HouseholdError.ExpiredInvitation(cause = this)
        "You are already in this household." -> HouseholdError.AlreadyJoined(cause = this)
        "This household is full." -> HouseholdError.HouseholdFull(cause = this)
        else -> toAppError()
    }
}
