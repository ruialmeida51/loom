package com.eyther.lumbridge.features.analytics.domain.model

sealed interface AnalyticsEventDomain {

    data class ScreenViewed(val screen: AnalyticsScreenDomain) : AnalyticsEventDomain

    data class ActionCompleted(val action: AnalyticsActionDomain) : AnalyticsEventDomain
}
