package com.eyther.lumbridge.features.settings.data.local.datasource

import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.diagnostics.domain.datasource.DiagnosticsDataSource
import com.eyther.lumbridge.features.settings.data.local.CommonUsageDataConsentStorage
import com.eyther.lumbridge.features.settings.data.local.UsageDataConsentStorage
import com.eyther.lumbridge.features.settings.data.local.model.UsageDataConsentDecision
import com.eyther.lumbridge.features.settings.domain.datasource.UsageDataConsentDataSource
import com.eyther.lumbridge.features.settings.domain.model.UsageDataPreferencesDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CommonUsageDataConsentDataSource
internal constructor(
    private val analytics: AnalyticsDataSource,
    private val diagnostics: DiagnosticsDataSource,
    private val storage: UsageDataConsentStorage,
) : UsageDataConsentDataSource {
    constructor(
        analytics: AnalyticsDataSource,
        diagnostics: DiagnosticsDataSource,
    ) : this(analytics, diagnostics, CommonUsageDataConsentStorage())

    private val mutex = Mutex()
    private val decision = MutableStateFlow(UsageDataConsentDecision())

    override fun stream() =
        combine(decision, analytics.stream(), diagnostics.stream()) {
            decision,
            analyticsPreferences,
            crashPreferences ->
            UsageDataPreferencesDomain(
                initialized = decision.initialized,
                answered = decision.answered,
                analyticsAvailable = analyticsPreferences.available,
                analyticsEnabled = analyticsPreferences.enabled,
                crashReportsAvailable = crashPreferences.available,
                crashReportsEnabled = crashPreferences.enabled,
            )
        }

    override suspend fun initialize(): AppResult<Unit> = safeApiCall {
        mutex.withLock {
            if (decision.value.initialized) return@withLock
            val answered = storage.isAnswered()
            analytics.initialize().getOrThrow()
            diagnostics.initialize().getOrThrow()
            decision.value = UsageDataConsentDecision(initialized = true, answered = answered)
        }
    }

    override suspend fun save(analytics: Boolean, crashReports: Boolean): AppResult<Unit> =
        safeApiCall {
            mutex.withLock {
                this@CommonUsageDataConsentDataSource.analytics
                    .setConsent(enabled = analytics)
                    .getOrThrow()
                diagnostics.setConsent(enabled = crashReports).getOrThrow()
                storage.markAnswered()
                decision.update { oldDecision -> oldDecision.copy(answered = true) }
            }
        }
}
