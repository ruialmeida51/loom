package com.eyther.lumbridge.features.analytics.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.analytics.generated.resources.Res
import com.eyther.lumbridge.features.analytics.generated.resources.analytics_consent_failed

fun AppError.toAnalyticsTextResource(): TextResource =
    TextResource.Resource(Res.string.analytics_consent_failed)
