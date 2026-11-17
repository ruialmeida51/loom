package com.eyther.lumbridge.features.tasks.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class TaskError(cause: Throwable? = null) : AppError(cause) {

    class InvalidTitle(cause: Throwable? = null) : TaskError(cause)

    class InvalidDateTime(cause: Throwable? = null) : TaskError(cause)

    class DateRequired(cause: Throwable? = null) : TaskError(cause)

    class AccessDenied(cause: Throwable? = null) : TaskError(cause)
}
