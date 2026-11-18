package com.eyther.lumbridge.features.authentication.presentation

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.SessionFixture
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import com.eyther.lumbridge.features.authentication.presentation.model.SessionStageUi
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SessionScreenViewModelTest {
    @Test
    fun signOutWhileBackgroundedClearsTheCachedPortalBeforeUiSubscribersReturn() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        fixture.authentication.stream().value = authenticated("you")
        val viewModel = fixture.viewModel()
        try {
            val ui = backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
            ui.cancel()
            advanceTimeBy(6_000)
            runCurrent()
            fixture.authentication.stream().value = AuthenticationStateDomain.SignedOut
            runCurrent()
            assertEquals(SessionStageUi.SIGNED_OUT, viewModel.state.value.stage)
            assertNull(viewModel.state.value.context)
            assertNull(viewModel.state.value.userId)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun anUnselectedHouseholdKeepsTheSetupFlowAcrossTokenRefresh() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        fixture.selected.remove("you")
        fixture.authentication.stream().value = authenticated("you")
        val viewModel = fixture.viewModel()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
            assertNull(viewModel.state.value.context)
            fixture.authentication.stream().value =
                AuthenticationStateDomain.Restoring(reconnecting = true)
            runCurrent()
            assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
            fixture.authentication.stream().value = authenticated("you", "refreshed@example.com")
            runCurrent()
            assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
            assertNull(viewModel.state.value.context)
            assertEquals(0, fixture.restoreCalls)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun tokenRefreshAndReconnectionKeepTheHouseholdAndDoNotRepeatRestoration() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        val viewModel = fixture.viewModel()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            assertEquals(SessionStageUi.SIGNED_OUT, viewModel.state.value.stage)
            fixture.authentication.stream().value = authenticated("you")
            runCurrent()
            assertEquals(fixture.home.id, viewModel.state.value.context?.id)
            viewModel.onHouseholdSelected(fixture.trip)
            runCurrent()
            fixture.authentication.stream().value =
                AuthenticationStateDomain.Restoring(reconnecting = true)
            runCurrent()
            assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
            assertEquals(fixture.trip.id, viewModel.state.value.context?.id)
            fixture.authentication.stream().value = authenticated("you", "refreshed@example.com")
            runCurrent()
            assertEquals(1, fixture.restoreCalls)
            assertEquals(fixture.trip.id, viewModel.state.value.context?.id)
            assertFalse(viewModel.state.value.reconnecting)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun aLateRestoreCannotReplaceAnotherUsersHousehold() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        val gate = CompletableDeferred<AppResult<List<HouseholdDomain>>>()
        fixture.restoreGate = gate
        fixture.authentication.stream().value = authenticated("you")
        val viewModel = fixture.viewModel()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            assertEquals(SessionStageUi.RESTORING, viewModel.state.value.stage)
            fixture.authentication.stream().value = AuthenticationStateDomain.SignedOut
            runCurrent()
            fixture.restoreGate = null
            fixture.authentication.stream().value = authenticated("another")
            runCurrent()
            assertEquals(fixture.trip.id, viewModel.state.value.context?.id)
            gate.complete(AppResult.Success(emptyList()))
            runCurrent()
            assertEquals(fixture.home.id, fixture.selected["you"])
            runCurrent()
            assertEquals("another", viewModel.state.value.userId)
            assertEquals(fixture.trip.id, viewModel.state.value.context?.id)
        } finally {
            gate.complete(AppResult.Success(emptyList()))
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun restoreAndPersistenceFailuresAreVisibleAndDoNotSilentlyLoseTheSelectedHousehold() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val fixture = SessionFixture()
            fixture.authentication.stream().value = authenticated("you")
            fixture.restoreResult = AppResult.Failure(CommonError.NoConnection())
            val viewModel = fixture.viewModel()
            try {
                backgroundScope.launch { viewModel.state.collect() }
                runCurrent()
                assertEquals(SessionStageUi.HOUSEHOLD_RESTORE_FAILED, viewModel.state.value.stage)
                assertNotNull(viewModel.state.value.restoreError)
                fixture.restoreResult = AppResult.Success(listOf(fixture.home))
                viewModel.onRetryHouseholdRestore()
                runCurrent()
                assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
                assertNull(viewModel.state.value.restoreError)
                fixture.selectionResult = AppResult.Failure(CommonError.LocalStorage())
                viewModel.onHouseholdSelected(fixture.trip)
                runCurrent()
                assertEquals(fixture.trip.id, viewModel.state.value.context?.id)
                assertNotNull(viewModel.state.value.selectionError)
                viewModel.onDismissSelectionError()
                assertNull(viewModel.state.value.selectionError)
            } finally {
                viewModel.viewModelScope.cancel()
                Dispatchers.resetMain()
            }
        }

    @Test
    fun rapidSelectionsWaitForAnUncancellableDeviceWriteAndPersistTheLatestChoice() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        fixture.authentication.stream().value = authenticated("you")
        val gate = CompletableDeferred<Unit>()
        val viewModel = fixture.viewModel()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            fixture.selectionGate = gate
            viewModel.onHouseholdSelected(fixture.trip)
            runCurrent()
            viewModel.onHouseholdSelected(fixture.home)
            viewModel.onHouseholdSelected(fixture.other)
            fixture.selectionGate = null
            runCurrent()
            assertEquals(listOf<String?>(fixture.trip.id), fixture.startedSelections.toList())
            assertEquals(fixture.other.id, viewModel.state.value.context?.id)
            gate.complete(Unit)
            runCurrent()
            assertEquals(
                listOf<String?>(fixture.trip.id, fixture.other.id),
                fixture.startedSelections.toList(),
            )
            assertEquals(fixture.other.id, fixture.selected["you"])
        } finally {
            gate.complete(Unit)
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun failedSignOutKeepsTheSessionAndCanBeRetriedWithoutDuplicateRequests() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        fixture.authentication.stream().value = authenticated("you")
        val gate = CompletableDeferred<AppResult<Unit>>()
        fixture.authentication.signOutGate = gate
        val viewModel = fixture.viewModel()
        try {
            backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            viewModel.onSignOut()
            viewModel.onSignOut()
            runCurrent()
            assertTrue(viewModel.state.value.signingOut)
            assertEquals(1, fixture.authentication.signOutCalls)
            gate.complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
            assertEquals(fixture.home.id, viewModel.state.value.context?.id)
            assertNotNull(viewModel.state.value.signOutError)
            viewModel.onSignOutDismissed()
            assertNull(viewModel.state.value.signOutError)
            fixture.authentication.signOutGate = null
            viewModel.onSignOut()
            runCurrent()
            assertEquals(SessionStageUi.SIGNED_OUT, viewModel.state.value.stage)
            assertNull(viewModel.state.value.context)
        } finally {
            gate.complete(AppResult.Success(Unit))
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    private fun authenticated(
        userId: String,
        email: String = "$userId@example.com",
    ): AuthenticationStateDomain =
        AuthenticationStateDomain.Authenticated(AuthSessionDomain(userId = userId, email = email))
}
