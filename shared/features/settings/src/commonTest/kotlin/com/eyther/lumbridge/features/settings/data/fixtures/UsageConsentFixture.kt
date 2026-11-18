package com.eyther.lumbridge.features.settings.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import com.eyther.lumbridge.features.diagnostics.domain.model.DiagnosticsPreferencesDomain
import com.eyther.lumbridge.features.settings.domain.datasource.UsageDataConsentDataSource
import com.eyther.lumbridge.features.settings.domain.model.UsageDataPreferencesDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal class UsageConsentFixture(previousAnalytics: Boolean = false) {
    var answered = false
    var saveCalls = 0
    var saveResult =
        CompletableDeferred<AppResult<Unit>>().apply { complete(AppResult.Success(Unit)) }
    val analyticsPreferences =
        MutableStateFlow(AnalyticsPreferencesDomain(available = true, enabled = previousAnalytics))
    val crashPreferences = MutableStateFlow(DiagnosticsPreferencesDomain(available = true))
    private val preferences =
        MutableStateFlow(
            UsageDataPreferencesDomain(
                initialized = true,
                analyticsAvailable = true,
                analyticsEnabled = previousAnalytics,
                crashReportsAvailable = true,
            ),
        )
    val source =
        object : UsageDataConsentDataSource {
            override fun stream() = preferences

            override suspend fun initialize() = AppResult.Success(Unit)

            override suspend fun save(analytics: Boolean, crashReports: Boolean): AppResult<Unit> {
                saveCalls++
                val result = saveResult.await()
                if (result is AppResult.Success) {
                    answered = true
                    analyticsPreferences.value =
                        analyticsPreferences.value.copy(enabled = analytics)
                    crashPreferences.value = crashPreferences.value.copy(enabled = crashReports)
                    preferences.value =
                        preferences.value.copy(
                            answered = true,
                            analyticsEnabled = analytics,
                            crashReportsEnabled = crashReports,
                        )
                }
                return result
            }
        }
}
