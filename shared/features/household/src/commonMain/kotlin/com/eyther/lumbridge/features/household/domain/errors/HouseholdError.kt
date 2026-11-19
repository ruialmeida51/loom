package com.eyther.lumbridge.features.household.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class HouseholdError(cause: Throwable? = null) : AppError(cause) {

    class ShareUnavailable(cause: Throwable? = null) : HouseholdError(cause)

    class AccessDenied(cause: Throwable? = null) : HouseholdError(cause)

    class Changed(cause: Throwable? = null) : HouseholdError(cause)

    class LastOwner(cause: Throwable? = null) : HouseholdError(cause)

    class NameConfirmation(cause: Throwable? = null) : HouseholdError(cause)

    class InvitationChanged(cause: Throwable? = null) : HouseholdError(cause)

    class InvitationFailed(cause: Throwable? = null) : HouseholdError(cause)

    class InvitationRateLimited(cause: Throwable? = null) : HouseholdError(cause)

    class InvalidInvitation(cause: Throwable? = null) : HouseholdError(cause)

    class InvalidName(cause: Throwable? = null) : HouseholdError(cause)

    class UnsupportedCurrency(cause: Throwable? = null) : HouseholdError(cause)

    class ExpiredInvitation(cause: Throwable? = null) : HouseholdError(cause)

    class AlreadyJoined(cause: Throwable? = null) : HouseholdError(cause)

    class HouseholdFull(cause: Throwable? = null) : HouseholdError(cause)

    class OwnerRequired(cause: Throwable? = null) : HouseholdError(cause)
}
