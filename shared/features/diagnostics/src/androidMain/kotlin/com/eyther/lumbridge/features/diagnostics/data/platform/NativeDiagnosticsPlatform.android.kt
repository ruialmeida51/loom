package com.eyther.lumbridge.features.diagnostics.data.platform

import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

internal actual object NativeDiagnosticsPlatform : DiagnosticsPlatform {
    actual override val available: Boolean
        get() = FirebaseApp.getApps(PlatformStorage.applicationContext).isNotEmpty()

    actual override fun setCollectionEnabled(enabled: Boolean) {
        if (available) FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enabled)
    }

    actual override suspend fun deleteUnsentReports() {
        if (available) FirebaseCrashlytics.getInstance().deleteUnsentReports()
    }
}
