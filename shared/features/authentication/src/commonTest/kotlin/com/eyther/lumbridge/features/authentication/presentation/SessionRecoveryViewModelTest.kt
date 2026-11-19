package com.eyther.lumbridge.features.authentication.presentation

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.SessionFixture
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import com.eyther.lumbridge.features.authentication.presentation.model.SessionStageUi
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SessionRecoveryViewModelTest {
    @Test
    fun resumeRepairsAnOpenSessionSilentlyAndPauseCancelsThePendingRepair() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        fixture.authentication.stream().value =
            AuthenticationStateDomain.Authenticated(
                AuthSessionDomain(userId = "you", email = "member@example.test"),
            )
        fixture.authentication.recoveryResult = AppResult.Failure(CommonError.NoConnection())
        val viewModel = fixture.viewModel()
        try {
            runCurrent()
            val selected = viewModel.state.value.context
            viewModel.onResume()
            viewModel.onPause()
            advanceTimeBy(250L)
            runCurrent()
            assertEquals(0, fixture.authentication.recoveryCalls)
            viewModel.onResume()
            viewModel.onResume()
            advanceTimeBy(250L)
            runCurrent()
            assertEquals(1, fixture.authentication.recoveryCalls)
            assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
            assertEquals(selected, viewModel.state.value.context)
            assertNull(viewModel.state.value.sessionRecoveryError)
            assertNull(viewModel.state.value.accessError)
            assertEquals(1, fixture.restoreCalls)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun stalledStartupOffersRetryAndAFailedRetryDoesNotDiscardTheSession() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        fixture.authentication.stream().value = AuthenticationStateDomain.Restoring()
        fixture.authentication.recoveryResult = AppResult.Failure(CommonError.NoConnection())
        val viewModel = fixture.viewModel()
        try {
            runCurrent()
            assertFalse(viewModel.state.value.sessionRecoveryAvailable)
            advanceTimeBy(15_000L)
            runCurrent()
            assertTrue(viewModel.state.value.sessionRecoveryAvailable)
            viewModel.onRetrySessionRecovery()
            runCurrent()
            assertEquals(SessionStageUi.RESTORING, viewModel.state.value.stage)
            assertNotNull(viewModel.state.value.sessionRecoveryError)
            assertFalse(viewModel.state.value.recoveringSession)
            assertEquals(1, fixture.authentication.recoveryCalls)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun duplicateRetriesAreBlockedAndLateFailureCannotReplaceTheRecoveredHousehold() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        fixture.authentication.stream().value =
            AuthenticationStateDomain.Restoring(reconnecting = true)
        val recovery = CompletableDeferred<AppResult<Unit>>()
        fixture.authentication.recoveryGate = recovery
        val viewModel = fixture.viewModel()
        try {
            runCurrent()
            viewModel.onRetrySessionRecovery()
            viewModel.onRetrySessionRecovery()
            runCurrent()
            assertEquals(1, fixture.authentication.recoveryCalls)
            assertTrue(viewModel.state.value.recoveringSession)
            fixture.authentication.stream().value =
                AuthenticationStateDomain.Authenticated(
                    AuthSessionDomain(userId = "you", email = "member@example.test"),
                )
            runCurrent()
            assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
            assertEquals(fixture.home.id, viewModel.state.value.context?.id)
            recovery.complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertEquals(SessionStageUi.READY, viewModel.state.value.stage)
            assertFalse(viewModel.state.value.recoveringSession)
            assertNull(viewModel.state.value.sessionRecoveryError)
            assertEquals(1, fixture.restoreCalls)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun revocationDuringRecoveryShowsLoginAndIgnoresTheOldRetryResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val fixture = SessionFixture()
        fixture.authentication.stream().value =
            AuthenticationStateDomain.Restoring(reconnecting = true)
        val recovery = CompletableDeferred<AppResult<Unit>>()
        fixture.authentication.recoveryGate = recovery
        val viewModel = fixture.viewModel()
        try {
            runCurrent()
            viewModel.onRetrySessionRecovery()
            runCurrent()
            fixture.authentication.stream().value = AuthenticationStateDomain.SignedOut
            runCurrent()
            recovery.complete(AppResult.Failure(CommonError.NotAuthenticated()))
            runCurrent()
            assertEquals(SessionStageUi.SIGNED_OUT, viewModel.state.value.stage)
            assertNull(viewModel.state.value.userId)
            assertNull(viewModel.state.value.context)
            assertNull(viewModel.state.value.sessionRecoveryError)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}
