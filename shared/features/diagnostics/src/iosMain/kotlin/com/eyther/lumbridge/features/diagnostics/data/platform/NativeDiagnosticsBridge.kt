package com.eyther.lumbridge.features.diagnostics.data.platform

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import kotlin.native.terminateWithUnhandledException

object NativeDiagnosticsBridge {
    internal var reporter: IOSCrashReporter? = null
        private set

    /** Preserve fatal termination after recording Kotlin frames for the native crash report. */
    @OptIn(ExperimentalNativeApi::class)
    fun initialize(reporter: IOSCrashReporter) {
        this.reporter = reporter
        setUnhandledExceptionHook { throwable ->
            try {
                reporter.recordUncaughtException(
                    name = throwable::class.simpleName ?: "KotlinException",
                    stackTrace = throwable.getStackTrace().toList(),
                )
            } finally {
                terminateWithUnhandledException(throwable)
            }
        }
    }
}
