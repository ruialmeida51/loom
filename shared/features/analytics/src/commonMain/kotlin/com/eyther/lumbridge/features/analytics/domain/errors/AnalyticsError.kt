package com.eyther.lumbridge.features.analytics.domain.errors

import com.eyther.lumbridge.core.domain.errors.AppError

sealed class AnalyticsError(cause: Throwable? = null) : AppError(cause) {

    class Unavailable : AnalyticsError()

    class ConsentUpdateFailed(cause: Throwable? = null) : AnalyticsError(cause)
}
