package com.eyther.lumbridge.features.settings.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.errors.AnalyticsError
import com.eyther.lumbridge.features.settings.data.fixtures.UsageConsentFixture
import com.eyther.lumbridge.features.settings.domain.usecase.GetUsageDataPreferencesStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.InitializeUsageDataConsentUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SaveUsageDataConsentUseCase
import com.eyther.lumbridge.features.settings.presentation.viewmodel.UsageDataConsentScreenViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class UsageDataConsentTest {
    private fun viewModel(fixture: UsageConsentFixture) =
        UsageDataConsentScreenViewModel(
            getPreferences = GetUsageDataPreferencesStreamUseCase(fixture.source),
            initializeConsent = InitializeUsageDataConsentUseCase(fixture.source),
            saveConsent = SaveUsageDataConsentUseCase(fixture.source),
        )

    @Test
    fun optionalChoicesStayOffUntilSaveAndTheDecisionSurvivesANewViewModel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = UsageConsentFixture()
        val store = ViewModelStore()
        val vm = viewModel(fixture).also { store.put("consent", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
            vm.onStartup()
            runCurrent()
            assertTrue(vm.state.value.visible)
            assertFalse(vm.state.value.analytics)
            assertFalse(vm.state.value.crashReports)
            vm.onCrashReportsChanged(true)
            runCurrent()
            assertFalse(fixture.crashPreferences.value.enabled)
            assertEquals(0, fixture.saveCalls)
            vm.onSave()
            runCurrent()
            assertTrue(fixture.answered)
            assertTrue(fixture.crashPreferences.value.enabled)
            assertFalse(fixture.analyticsPreferences.value.enabled)
            assertFalse(vm.state.value.visible)
            vm.onStartup()
            runCurrent()
            assertFalse(vm.state.value.visible)
            val restored = viewModel(fixture).also { store.put("restored", it) }
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                restored.state.collect()
            }
            restored.onStartup()
            runCurrent()
            assertFalse(restored.state.value.visible)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun dismissingUnansweredConsentKeepsPreviousAnalyticsButNeverEnablesCrashReports() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = UsageConsentFixture(previousAnalytics = true)
        val store = ViewModelStore()
        val vm = viewModel(fixture).also { store.put("consent", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
            vm.onStartup()
            runCurrent()
            assertTrue(vm.state.value.analytics)
            assertFalse(vm.state.value.crashReports)
            vm.onAnalyticsChanged(false)
            vm.onCrashReportsChanged(true)
            runCurrent()
            vm.onDismiss()
            runCurrent()
            assertTrue(fixture.analyticsPreferences.value.enabled)
            assertFalse(fixture.crashPreferences.value.enabled)
            assertTrue(fixture.answered)
            vm.onOpen()
            runCurrent()
            vm.onAnalyticsChanged(false)
            runCurrent()
            vm.onSave()
            runCurrent()
            assertFalse(fixture.analyticsPreferences.value.enabled)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun savingBlocksDuplicateActionsAndAFailureRetainsTheChoicesForRetry() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = UsageConsentFixture().apply { saveResult = CompletableDeferred() }
        val store = ViewModelStore()
        val vm = viewModel(fixture).also { store.put("consent", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.state.collect() }
            vm.onStartup()
            runCurrent()
            vm.onAnalyticsChanged(true)
            runCurrent()
            vm.onSave()
            runCurrent()
            vm.onAllowAll()
            vm.onDismiss()
            runCurrent()
            assertEquals(1, fixture.saveCalls)
            assertTrue(vm.state.value.busy)
            fixture.saveResult.complete(AppResult.Failure(AnalyticsError.ConsentUpdateFailed()))
            runCurrent()
            assertTrue(vm.state.value.visible)
            assertTrue(vm.state.value.analytics)
            assertNotNull(vm.state.value.error)
            assertFalse(fixture.answered)
            fixture.saveResult = CompletableDeferred(AppResult.Success(Unit))
            vm.onSave()
            runCurrent()
            assertFalse(vm.state.value.visible)
            assertTrue(fixture.answered)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
