package com.eyther.lumbridge.features.expenses.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class RecurringExpenseError(cause: Throwable? = null) : AppError(cause) {

    class Cancelled(cause: Throwable? = null) : RecurringExpenseError(cause)

    class NotFound(cause: Throwable? = null) : RecurringExpenseError(cause)

    class PermissionDenied(cause: Throwable? = null) : RecurringExpenseError(cause)

    class InvalidAction(cause: Throwable? = null) : RecurringExpenseError(cause)

    class Changed(cause: Throwable? = null) : RecurringExpenseError(cause)

    class InactiveParticipants(cause: Throwable? = null) : RecurringExpenseError(cause)

    class InvalidDate(cause: Throwable? = null) : RecurringExpenseError(cause)

    class InvalidAnchorDay(cause: Throwable? = null) : RecurringExpenseError(cause)
}
