package com.eyther.lumbridge.features.insights.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class InsightsError(cause: Throwable? = null) : AppError(cause) {

    class InvalidMonth(cause: Throwable? = null) : InsightsError(cause)

    class ExportFailed(cause: Throwable? = null) : InsightsError(cause)
}
