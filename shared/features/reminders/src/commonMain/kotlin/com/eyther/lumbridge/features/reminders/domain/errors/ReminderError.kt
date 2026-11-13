package com.eyther.lumbridge.features.reminders.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class ReminderError(cause: Throwable? = null) : AppError(cause) {

    class InvalidTitle(cause: Throwable? = null) : ReminderError(cause)

    class InvalidRecurrence(cause: Throwable? = null) : ReminderError(cause)

    class InvalidDateTime(cause: Throwable? = null) : ReminderError(cause)

    class AccessDenied(cause: Throwable? = null) : ReminderError(cause)
}
