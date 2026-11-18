package com.eyther.lumbridge.features.analytics.data.local.datasource

import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.data.local.AnalyticsConsentStorage
import com.eyther.lumbridge.features.analytics.data.local.CommonAnalyticsConsentStorage
import com.eyther.lumbridge.features.analytics.data.mapper.toAnalyticsError
import com.eyther.lumbridge.features.analytics.data.mapper.toRequest
import com.eyther.lumbridge.features.analytics.data.platform.AnalyticsPlatform
import com.eyther.lumbridge.features.analytics.data.platform.NativeAnalyticsPlatform
import com.eyther.lumbridge.features.analytics.domain.datasource.AnalyticsDataSource
import com.eyther.lumbridge.features.analytics.domain.errors.AnalyticsError
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CommonAnalyticsDataSource
internal constructor(
    private val platform: AnalyticsPlatform,
    private val storage: AnalyticsConsentStorage,
) : AnalyticsDataSource {

    constructor() :
        this(
            platform = NativeAnalyticsPlatform,
            storage = CommonAnalyticsConsentStorage(),
        )

    private val consentMutex = Mutex()
    private var initialized = false
    private val mutablePreferences =
        MutableStateFlow(
            AnalyticsPreferencesDomain(available = platform.available),
        )

    override fun stream(): StateFlow<AnalyticsPreferencesDomain> = mutablePreferences.asStateFlow()

    override suspend fun initialize(): AppResult<Unit> =
        safeApiCall(
            mapError = Exception::toAnalyticsError,
        ) {
            consentMutex.withLock {
                if (initialized) return@withLock
                val enabled =
                    try {
                        platform.available && storage.get()
                    } catch (cause: Exception) {
                        platform.setConsent(enabled = false)
                        throw cause
                    }
                platform.setConsent(enabled = enabled)
                mutablePreferences.value =
                    AnalyticsPreferencesDomain(
                        available = platform.available,
                        enabled = enabled,
                    )
                initialized = true
            }
        }

    override suspend fun setConsent(enabled: Boolean): AppResult<Unit> =
        safeApiCall(
            mapError = Exception::toAnalyticsError,
        ) {
            consentMutex.withLock {
                if (enabled && !platform.available) {
                    throw AnalyticsError.Unavailable()
                }
                if (!enabled) {
                    platform.setConsent(enabled = false)
                    mutablePreferences.value =
                        AnalyticsPreferencesDomain(available = platform.available)
                    platform.clearData()
                }
                storage.set(enabled = enabled)
                platform.setConsent(enabled = enabled)
                mutablePreferences.value =
                    AnalyticsPreferencesDomain(
                        available = platform.available,
                        enabled = enabled,
                    )
                initialized = true
            }
        }

    override suspend fun track(event: AnalyticsEventDomain): AppResult<Unit> = safeApiCall {
        consentMutex.withLock {
            if (mutablePreferences.value.enabled) {
                platform.logEvent(event = event.toRequest())
            }
        }
    }
}
