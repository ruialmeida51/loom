package com.eyther.lumbridge.features.analytics.data.platform

object NativeAnalyticsBridge {
    internal var reporter: IOSAnalyticsReporter? = null
        private set

    fun initialize(reporter: IOSAnalyticsReporter) {
        this.reporter = reporter
    }
}
