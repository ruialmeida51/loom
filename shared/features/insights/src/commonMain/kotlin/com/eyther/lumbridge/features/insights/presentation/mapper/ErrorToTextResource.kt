package com.eyther.lumbridge.features.insights.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.insights.domain.errors.InsightsError
import com.eyther.lumbridge.features.insights.generated.resources.Res
import com.eyther.lumbridge.features.insights.generated.resources.error_export_failed
import com.eyther.lumbridge.features.insights.generated.resources.error_invalid_month

fun AppError.toInsightsTextResource(): TextResource = when (this) {
    is InsightsError.InvalidMonth -> TextResource.Resource(Res.string.error_invalid_month)
    is InsightsError.ExportFailed -> TextResource.Resource(Res.string.error_export_failed)
    else -> toTextResource()
}
