package com.eyther.lumbridge.features.diagnostics.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.features.diagnostics.data.local.DiagnosticsConsentStorage
import com.eyther.lumbridge.features.diagnostics.data.local.datasource.CommonDiagnosticsDataSource
import com.eyther.lumbridge.features.diagnostics.data.platform.DiagnosticsPlatform
import com.eyther.lumbridge.features.diagnostics.domain.errors.DiagnosticsError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class DiagnosticsConsentTest {
    private class Platform(override val available: Boolean = true) : DiagnosticsPlatform {
        val calls = mutableListOf<String>()
        var enabled = false

        override fun setCollectionEnabled(enabled: Boolean) {
            this.enabled = enabled
            calls += "collect:$enabled"
        }

        override suspend fun deleteUnsentReports() {
            calls += "delete"
        }
    }

    private class Storage(var enabled: Boolean = false) : DiagnosticsConsentStorage {
        var failWrites = false

        override fun get() = enabled

        override fun set(enabled: Boolean) {
            check(!failWrites)
            this.enabled = enabled
        }
    }

    @Test
    fun optionalCollectionIsOffAndOldReportsAreDeletedBeforeOptIn() = runTest {
        val platform = Platform()
        val storage = Storage()
        val source = CommonDiagnosticsDataSource(platform = platform, storage = storage)
        source.initialize().getOrThrow()
        assertFalse(platform.enabled)
        platform.calls.clear()
        source.setConsent(enabled = true).getOrThrow()
        assertEquals(listOf("delete", "collect:true"), platform.calls)
        assertTrue(storage.enabled)
        platform.calls.clear()
        source.setConsent(enabled = false).getOrThrow()
        assertEquals("collect:false", platform.calls.first())
        assertTrue("delete" in platform.calls)
        assertFalse(source.stream().value.enabled)
    }

    @Test
    fun persistedConsentRestoresValidCrashReportsAndWithdrawalStopsBeforeAFailedSave() = runTest {
        val platform = Platform()
        val storage = Storage(enabled = true)
        val source = CommonDiagnosticsDataSource(platform = platform, storage = storage)
        source.initialize().getOrThrow()
        assertTrue(platform.enabled)
        assertFalse("delete" in platform.calls)
        storage.failWrites = true
        assertIs<AppResult.Failure>(source.setConsent(enabled = false))
        assertFalse(platform.enabled)
        assertFalse(source.stream().value.enabled)
    }

    @Test
    fun reopeningPreferencesKeepsConsentedReportsAndDoesNotToggleCollection() = runTest {
        val platform = Platform()
        val source =
            CommonDiagnosticsDataSource(platform = platform, storage = Storage(enabled = true))
        source.initialize().getOrThrow()
        source.initialize().getOrThrow()
        assertEquals(listOf("collect:true"), platform.calls)
        assertTrue(source.stream().value.enabled)
    }

    @Test
    fun aFailedOptInCannotEnableTheSdkAndUnavailableBuildsRefuseIt() = runTest {
        val platform = Platform()
        val storage = Storage().apply { failWrites = true }
        val source = CommonDiagnosticsDataSource(platform = platform, storage = storage)
        assertIs<AppResult.Failure>(source.setConsent(enabled = true))
        assertFalse(platform.enabled)
        val unavailable =
            CommonDiagnosticsDataSource(platform = Platform(available = false), storage = Storage())
        assertIs<DiagnosticsError.Unavailable>(
            assertIs<AppResult.Failure>(unavailable.setConsent(enabled = true)).error,
        )
    }
}
