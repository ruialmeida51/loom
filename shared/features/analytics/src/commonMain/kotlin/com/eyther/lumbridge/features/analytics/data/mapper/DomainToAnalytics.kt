package com.eyther.lumbridge.features.analytics.data.mapper

import com.eyther.lumbridge.features.analytics.data.platform.model.AnalyticsEventRequest
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain

internal fun AnalyticsEventDomain.toRequest(): AnalyticsEventRequest = when (this) {
    is AnalyticsEventDomain.ScreenViewed -> AnalyticsEventRequest(
        name = "screen_view",
        parameters = mapOf("screen_name" to screen.name.lowercase()),
    )
    is AnalyticsEventDomain.ActionCompleted -> AnalyticsEventRequest(
        name = action.name.lowercase(),
        parameters = emptyMap(),
    )
}
