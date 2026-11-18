package com.eyther.lumbridge.features.settings.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.analytics.domain.errors.AnalyticsError
import com.eyther.lumbridge.features.diagnostics.domain.errors.DiagnosticsError
import com.eyther.lumbridge.features.settings.generated.resources.Res
import com.eyther.lumbridge.features.settings.generated.resources.consent_error
import com.eyther.lumbridge.features.settings.generated.resources.consent_unavailable

internal fun AppError.toUsageDataTextResource(): TextResource =
    when (this) {
        is DiagnosticsError.Unavailable,
        is AnalyticsError.Unavailable -> TextResource.Resource(Res.string.consent_unavailable)
        is AnalyticsError.ConsentUpdateFailed -> TextResource.Resource(Res.string.consent_error)
        else -> toTextResource()
    }
