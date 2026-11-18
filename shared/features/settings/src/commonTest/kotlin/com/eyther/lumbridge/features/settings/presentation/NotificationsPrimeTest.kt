package com.eyther.lumbridge.features.settings.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.data.fixtures.NotificationFixtureDataSource
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain
import com.eyther.lumbridge.features.settings.domain.usecase.GetNotificationPermissionUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.MarkNotificationPrimeAnsweredUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.OpenNotificationSettingsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.presentation.viewmodel.NotificationsPrimeScreenViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsPrimeTest {
    private fun viewModel(source: NotificationFixtureDataSource) =
        NotificationsPrimeScreenViewModel(
            getSettings = GetSettingsStreamUseCase(source),
            getPermission = GetNotificationPermissionUseCase(source),
            markAnswered = MarkNotificationPrimeAnsweredUseCase(source),
            openSettings = OpenNotificationSettingsUseCase(source),
            setNotifications = SetNotificationsUseCase(source),
        )

    @Test
    fun contextualExplanationNeverRequestsSystemPermissionAndNotNowPersists() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = NotificationFixtureDataSource()
        val store = ViewModelStore()
        val vm = viewModel(source).also { store.put("notifications", it) }
        try {
            vm.onContextRequested()
            runCurrent()
            assertTrue(vm.state.value.visible)
            assertEquals(0, source.registrationCalls)
            vm.onResume()
            runCurrent()
            assertEquals(0, source.registrationCalls)
            vm.onNotNow()
            runCurrent()
            assertTrue(source.stream().value.notificationPrimeAnswered)
            assertFalse(vm.state.value.visible)
            vm.onContextRequested()
            runCurrent()
            assertFalse(vm.state.value.visible)
            vm.onOpen()
            runCurrent()
            assertTrue(vm.state.value.visible)
            vm.onEnable()
            runCurrent()
            assertEquals(1, source.registrationCalls)
            assertFalse(vm.state.value.visible)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun deniedPermissionsOpenSettingsAndOnlyAnExplicitReturnRegistersTheDevice() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source =
            NotificationFixtureDataSource().apply {
                permission = NotificationPermissionDomain.DENIED
            }
        val store = ViewModelStore()
        val vm = viewModel(source).also { store.put("notifications", it) }
        try {
            vm.onContextRequested()
            runCurrent()
            assertFalse(vm.state.value.visible)
            vm.onOpen()
            runCurrent()
            vm.onEnable()
            runCurrent()
            assertEquals(1, source.settingsCalls)
            assertEquals(0, source.registrationCalls)
            vm.onResume()
            runCurrent()
            assertEquals(0, source.registrationCalls)
            source.permission = NotificationPermissionDomain.GRANTED
            source.registration = CompletableDeferred()
            vm.onResume()
            runCurrent()
            vm.onEnable()
            vm.onNotNow()
            runCurrent()
            assertEquals(1, source.registrationCalls)
            assertTrue(vm.state.value.busy)
            source.registration.complete(
                AppResult.Success(Unit),
            )
            runCurrent()
            assertFalse(vm.state.value.visible)
            assertTrue(source.stream().value.notifications)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
