package com.eyther.lumbridge.features.expenses.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class ExpenseAccountError(cause: Throwable? = null) : AppError(cause) {
    class InvalidName(cause: Throwable? = null) : ExpenseAccountError(cause)

    class InvalidMembers(cause: Throwable? = null) : ExpenseAccountError(cause)

    class Archived(cause: Throwable? = null) : ExpenseAccountError(cause)

    class PermissionDenied(cause: Throwable? = null) : ExpenseAccountError(cause)

    class Changed(cause: Throwable? = null) : ExpenseAccountError(cause)

    class NotFound(cause: Throwable? = null) : ExpenseAccountError(cause)

    class InvalidGuestName(cause: Throwable? = null) : ExpenseAccountError(cause)

    class GuestLimit(cause: Throwable? = null) : ExpenseAccountError(cause)

    class GuestsNotOnEveryday(cause: Throwable? = null) : ExpenseAccountError(cause)

    class GuestHasBalance(cause: Throwable? = null) : ExpenseAccountError(cause)

    class GuestsUnavailable(cause: Throwable? = null) : ExpenseAccountError(cause)
}
