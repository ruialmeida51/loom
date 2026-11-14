package com.eyther.lumbridge.features.analytics.data.platform.model

internal data class AnalyticsEventRequest(
    val name: String,
    val parameters: Map<String, String>,
)
