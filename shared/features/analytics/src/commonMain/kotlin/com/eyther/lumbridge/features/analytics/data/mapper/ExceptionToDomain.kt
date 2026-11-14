package com.eyther.lumbridge.features.analytics.data.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.analytics.domain.errors.AnalyticsError

internal fun Exception.toAnalyticsError(): AppError = when (this) {
    is AppError -> this
    else -> AnalyticsError.ConsentUpdateFailed(cause = this)
}
