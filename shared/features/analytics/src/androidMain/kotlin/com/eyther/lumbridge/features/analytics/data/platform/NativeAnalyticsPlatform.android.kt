package com.eyther.lumbridge.features.analytics.data.platform

import android.os.Bundle
import com.eyther.lumbridge.core.data.local.PlatformStorage
import com.eyther.lumbridge.features.analytics.data.platform.model.AnalyticsEventRequest
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

internal actual object NativeAnalyticsPlatform : AnalyticsPlatform {

    actual override val available: Boolean
        get() = FirebaseApp.getApps(PlatformStorage.applicationContext).isNotEmpty()

    actual override fun setConsent(enabled: Boolean) {
        if (!available) return
        val analytics = FirebaseAnalytics.getInstance(PlatformStorage.applicationContext)
        val analyticsConsent = if (enabled) {
            FirebaseAnalytics.ConsentStatus.GRANTED
        } else {
            FirebaseAnalytics.ConsentStatus.DENIED
        }
        analytics.setConsent(
            mapOf(
                FirebaseAnalytics.ConsentType.ANALYTICS_STORAGE to analyticsConsent,
                FirebaseAnalytics.ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.DENIED,
                FirebaseAnalytics.ConsentType.AD_PERSONALIZATION to FirebaseAnalytics.ConsentStatus.DENIED,
            ),
        )
        analytics.setAnalyticsCollectionEnabled(enabled)
    }

    actual override fun logEvent(event: AnalyticsEventRequest) {
        if (!available) return
        val parameters = Bundle().apply {
            event.parameters.forEach { (key, value) -> putString(key, value) }
        }
        FirebaseAnalytics.getInstance(PlatformStorage.applicationContext).logEvent(event.name, parameters)
    }

    actual override fun clearData() {
        if (available) FirebaseAnalytics.getInstance(PlatformStorage.applicationContext).resetAnalyticsData()
    }
}
