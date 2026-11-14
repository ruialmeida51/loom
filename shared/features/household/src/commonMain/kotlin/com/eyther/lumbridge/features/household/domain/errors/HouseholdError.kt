package com.eyther.lumbridge.features.household.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class HouseholdError(cause: Throwable? = null) : AppError(cause) {

    class InvalidInvitation(cause: Throwable? = null) : HouseholdError(cause)

    class InvalidName(cause: Throwable? = null) : HouseholdError(cause)

    class UnsupportedCurrency(cause: Throwable? = null) : HouseholdError(cause)

    class ExpiredInvitation(cause: Throwable? = null) : HouseholdError(cause)

    class AlreadyJoined(cause: Throwable? = null) : HouseholdError(cause)

    class HouseholdFull(cause: Throwable? = null) : HouseholdError(cause)

    class OwnerRequired(cause: Throwable? = null) : HouseholdError(cause)
}
