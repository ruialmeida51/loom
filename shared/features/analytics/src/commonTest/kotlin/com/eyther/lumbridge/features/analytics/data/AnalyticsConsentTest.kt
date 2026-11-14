package com.eyther.lumbridge.features.analytics.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.analytics.data.local.AnalyticsConsentStorage
import com.eyther.lumbridge.features.analytics.data.local.datasource.CommonAnalyticsDataSource
import com.eyther.lumbridge.features.analytics.data.mapper.toRequest
import com.eyther.lumbridge.features.analytics.data.platform.AnalyticsPlatform
import com.eyther.lumbridge.features.analytics.data.platform.model.AnalyticsEventRequest
import com.eyther.lumbridge.features.analytics.domain.errors.AnalyticsError
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsActionDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsEventDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsScreenDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class AnalyticsConsentTest {

    private class Platform(override val available: Boolean = true) : AnalyticsPlatform {

        var enabled = false
        var resets = 0
        val events = mutableListOf<AnalyticsEventRequest>()

        override fun setConsent(enabled: Boolean) {
            this.enabled = enabled
        }

        override fun logEvent(event: AnalyticsEventRequest) {
            events += event
        }

        override fun clearData() {
            resets++
        }
    }

    private class Storage(var enabled: Boolean = false) : AnalyticsConsentStorage {

        var failReads = false
        var failWrites = false

        override fun get(): Boolean {
            if (failReads) throw IllegalStateException("storage unavailable")
            return enabled
        }

        override fun set(enabled: Boolean) {
            if (failWrites) throw IllegalStateException("storage unavailable")
            this.enabled = enabled
        }
    }

    @Test
    fun collectionStaysOffUntilExplicitConsentAndStopsAfterWithdrawal() = runTest {
        val platform = Platform()
        val storage = Storage()
        val source = CommonAnalyticsDataSource(platform = platform, storage = storage)
        val event = AnalyticsEventDomain.ScreenViewed(AnalyticsScreenDomain.HOME)

        source.initialize().getOrThrow()
        source.track(event).getOrThrow()
        assertFalse(platform.enabled)
        assertTrue(platform.events.isEmpty())

        source.setConsent(true).getOrThrow()
        source.track(event).getOrThrow()
        assertTrue(storage.enabled)
        assertEquals(1, platform.events.size)

        source.setConsent(false).getOrThrow()
        source.track(event).getOrThrow()
        assertFalse(platform.enabled)
        assertFalse(storage.enabled)
        assertFalse(source.stream().value.enabled)
        assertEquals(1, platform.resets)
        assertEquals(1, platform.events.size)
    }

    @Test
    fun storedConsentIsRestoredWithoutAnAccountOrHouseholdIdentifier() = runTest {
        val platform = Platform()
        val source = CommonAnalyticsDataSource(platform = platform, storage = Storage(enabled = true))
        source.initialize().getOrThrow()
        assertTrue(platform.enabled)
        assertTrue(source.stream().value.enabled)
    }

    @Test
    fun unavailablePlatformsCannotEnableCollection() = runTest {
        val source = CommonAnalyticsDataSource(platform = Platform(available = false), storage = Storage())
        val result = source.setConsent(true)
        assertIs<AnalyticsError.Unavailable>(assertIs<AppResult.Failure>(result).error)
        assertFalse(source.stream().value.enabled)
    }

    @Test
    fun aFailedConsentSaveCannotEnableCollection() = runTest {
        val platform = Platform()
        val storage = Storage().apply { failWrites = true }
        val source = CommonAnalyticsDataSource(platform = platform, storage = storage)
        val result = source.setConsent(true)
        assertIs<AnalyticsError.ConsentUpdateFailed>(assertIs<AppResult.Failure>(result).error)
        source.track(AnalyticsEventDomain.ScreenViewed(AnalyticsScreenDomain.HOME))
        assertFalse(platform.enabled)
        assertTrue(platform.events.isEmpty())
    }

    @Test
    fun withdrawalDisablesTheSdkEvenIfLocalPersistenceFails() = runTest {
        val platform = Platform()
        val storage = Storage(enabled = true)
        val source = CommonAnalyticsDataSource(platform = platform, storage = storage)
        source.initialize().getOrThrow()
        storage.failWrites = true
        assertIs<AppResult.Failure>(source.setConsent(false))
        source.track(AnalyticsEventDomain.ScreenViewed(AnalyticsScreenDomain.HOME))
        assertFalse(platform.enabled)
        assertFalse(source.stream().value.enabled)
        assertTrue(platform.events.isEmpty())
    }

    @Test
    fun anUnreadableConsentChoiceKeepsTheSdkDisabled() = runTest {
        val platform = Platform().apply { enabled = true }
        val storage = Storage(enabled = true).apply { failReads = true }
        val source = CommonAnalyticsDataSource(platform = platform, storage = storage)

        val result = source.initialize()
        source.track(AnalyticsEventDomain.ScreenViewed(AnalyticsScreenDomain.HOME))

        assertIs<AnalyticsError.ConsentUpdateFailed>(assertIs<AppResult.Failure>(result).error)
        assertFalse(platform.enabled)
        assertFalse(source.stream().value.enabled)
        assertTrue(platform.events.isEmpty())
    }

    @Test
    fun analyticsSchemaAllowsOnlyClosedScreenAndActionNames() {
        AnalyticsScreenDomain.entries.forEach { screen ->
            val request = AnalyticsEventDomain.ScreenViewed(screen).toRequest()
            assertEquals("screen_view", request.name)
            assertEquals(mapOf("screen_name" to screen.name.lowercase()), request.parameters)
        }
        AnalyticsActionDomain.entries.forEach { action ->
            val request = AnalyticsEventDomain.ActionCompleted(action).toRequest()
            assertTrue(request.name.matches(Regex("[a-z][a-z0-9_]{0,39}")))
            assertTrue(request.parameters.isEmpty())
        }
    }
}
