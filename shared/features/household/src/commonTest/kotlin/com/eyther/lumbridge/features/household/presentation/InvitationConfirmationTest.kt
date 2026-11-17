package com.eyther.lumbridge.features.household.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.data.fixtures.FakeHouseholdDataSource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RevokeInvitationUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdDetailsScreenViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class InvitationConfirmationTest {
    @Test
    fun revocationRequiresConfirmationAndKeepsTheCodeAndTypedFailureUntilRetry() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source =
            FakeHouseholdDataSource().also {
                it.members = listOf(MemberDomain(id = "you", name = "Rui", role = "owner"))
                it.invitationCode = "ABC234"
            }
        val store = ViewModelStore()
        val model =
            HouseholdDetailsScreenViewModel(
                    context =
                        HouseholdContextDomain(
                            id = "home",
                            userId = "you",
                            name = "Home",
                            currency = "EUR",
                            timeZone = "UTC",
                        ),
                    getMembersStreamUseCase = GetMembersStreamUseCase(source),
                    createInvitationUseCase = CreateInvitationUseCase(source),
                    revokeInvitationUseCase = RevokeInvitationUseCase(source),
                )
                .also { store.put("home", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            model.onCreateInvitation()
            runCurrent()
            assertEquals("ABC234", model.state.value.invitation)
            model.onRevokeInvitationConfirmed()
            runCurrent()
            assertEquals(0, source.revocationCalls)
            model.onRevokeInvitation()
            runCurrent()
            assertNotNull(model.state.value.revokeConfirmation)
            assertEquals(0, source.revocationCalls)
            model.onRevokeInvitationCancelled()
            assertEquals("ABC234", model.state.value.invitation)
            assertNull(model.state.value.revokeConfirmation)
            val gate = CompletableDeferred<AppResult<Unit>>()
            source.revocationGate = gate
            model.onRevokeInvitation()
            model.onRevokeInvitationConfirmed()
            runCurrent()
            model.onRevokeInvitationConfirmed()
            model.onRevokeInvitationCancelled()
            runCurrent()
            assertEquals(1, source.revocationCalls)
            gate.complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertFalse(model.state.value.busy)
            assertEquals("ABC234", model.state.value.invitation)
            assertNotNull(model.state.value.revokeError)
            assertNull(model.state.value.invitationError)
            assertNull(model.state.value.displayError)
            source.revocationGate = null
            model.onRevokeInvitationConfirmed()
            runCurrent()
            assertEquals(2, source.revocationCalls)
            assertNull(model.state.value.invitation)
            assertNull(model.state.value.revokeConfirmation)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
