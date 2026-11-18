package com.eyther.lumbridge.features.diagnostics.data.platform

/** Swift owns the Firebase SDK. No household, identity or expense data is attached to reports. */
interface IOSCrashReporter {
    fun setCollectionEnabled(enabled: Boolean)

    fun deleteUnsentReports()

    fun recordUncaughtException(name: String, stackTrace: List<String>)
}
