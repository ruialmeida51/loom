package com.eyther.lumbridge.features.household.presentation

import androidx.lifecycle.ViewModelStore
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.data.fixtures.FakeHouseholdManagementDataSource
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdManagementDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.domain.usecase.ChangeHouseholdMemberUseCase
import com.eyther.lumbridge.features.household.domain.usecase.DeleteHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdManagementStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.ReplaceHouseholdInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RevokeHouseholdInvitationUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdDetailsScreenViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class InvitationConfirmationTest {
    @Test
    fun persistedCodeNeedsNoCreationAndRevocationRetainsFrozenIdentityAndErrorForRetry() =
        withModel { model, source ->
            assertEquals("ABC234", model.state.value.invitation?.code)
            assertNull(model.state.value.confirmation)
            model.onConfirm()
            runCurrent()
            assertEquals(0, source.revocations.size)
            model.onRevokeInvitation()
            assertNotNull(model.state.value.confirmation)
            model.onConfirmationDismissed()
            assertNull(model.state.value.confirmation)
            assertEquals("ABC234", model.state.value.invitation?.code)
            val gate = CompletableDeferred<AppResult<Unit>>()
            source.gate = gate
            model.onRevokeInvitation()
            val target = model.state.value.confirmation
            model.onConfirm()
            runCurrent()
            model.onConfirm()
            model.onConfirmationDismissed()
            assertEquals(target, model.state.value.confirmation)
            assertEquals(1, source.revocations.size)
            source.snapshot.value =
                AppResult.Success(
                    HouseholdManagementDomain(
                        listOf(source.owner, source.member),
                        source.invitation.copy(id = "new-invite", version = 3),
                    ),
                )
            runCurrent()
            assertEquals(
                "invite",
                model.state.value.invitation?.id,
                "In-flight reads cannot replace a pending mutation",
            )
            gate.complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertFalse(model.state.value.busy)
            assertNotNull(model.state.value.error)
            assertEquals(target, model.state.value.confirmation)
            source.snapshot.value =
                AppResult.Success(
                    HouseholdManagementDomain(
                        listOf(source.owner, source.member),
                        source.invitation.copy(id = "new-invite", version = 3),
                    ),
                )
            runCurrent()
            source.gate = null
            model.onConfirm()
            runCurrent()
            assertEquals(2, source.revocations.size)
            assertEquals(
                source.revocations.first(),
                source.revocations.last(),
                "Retry keeps the original ID, version and request",
            )
            assertNull(model.state.value.invitation)
            assertNull(model.state.value.confirmation)
        }

    @Test
    fun memberConfirmationKeepsItsOriginalVersionWhenRealtimeChangesTheMember() =
        withModel { model, source ->
            source.result = AppResult.Failure(HouseholdError.Changed())
            model.onMemberAction(memberId = "alex", action = HouseholdMemberActionDomain.PROMOTE)
            assertEquals(0, source.memberChanges.size)
            val target = model.state.value.confirmation
            source.snapshot.value =
                AppResult.Success(
                    HouseholdManagementDomain(
                        listOf(source.owner, source.member.copy(version = 5)),
                        source.invitation,
                    ),
                )
            runCurrent()
            model.onConfirm()
            runCurrent()
            assertEquals(1, source.memberChanges.single().first.version)
            assertEquals(target, model.state.value.confirmation)
            assertNotNull(model.state.value.error)
            model.onConfirm()
            runCurrent()
            assertEquals(source.memberChanges.first(), source.memberChanges.last())
        }

    @Test
    fun lastOwnerCannotLeaveAndDeletionNeedsExactNameWithSafeBusyFailureRetry() =
        withModel { model, source ->
            model.onLeaveRequested()
            assertNull(model.state.value.confirmation)
            assertNotNull(model.state.value.error)
            assertTrue(source.memberChanges.isEmpty())
            model.onDeleteRequested()
            assertFalse(model.state.value.canConfirm)
            model.onConfirmationNameChanged("home")
            runCurrent()
            model.onConfirm()
            runCurrent()
            assertTrue(source.deletions.isEmpty())
            model.onConfirmationNameChanged("Home")
            runCurrent()
            assertTrue(model.state.value.canConfirm)
            val gate = CompletableDeferred<AppResult<Unit>>()
            source.gate = gate
            model.onConfirm()
            runCurrent()
            model.onConfirmationNameChanged("Other household")
            model.onConfirm()
            model.onConfirmationDismissed()
            assertEquals(1, source.deletions.size)
            assertEquals("Home", model.state.value.confirmationName)
            gate.complete(AppResult.Failure(CommonError.NoConnection()))
            runCurrent()
            assertNotNull(model.state.value.confirmation)
            assertNotNull(model.state.value.error)
            source.gate = null
            model.onConfirm()
            runCurrent()
            assertEquals(source.deletions.first(), source.deletions.last())
            assertTrue(model.state.value.exited)
            assertTrue(model.state.value.exitExplicit)
        }

    @Test
    fun losingOwnerPermissionDisablesAnAlreadyOpenConfirmation() = withModel { model, source ->
        model.onDeleteRequested()
        model.onConfirmationNameChanged("Home")
        runCurrent()
        assertTrue(model.state.value.canConfirm)
        source.snapshot.value =
            AppResult.Success(
                HouseholdManagementDomain(
                    members =
                        listOf(
                            source.owner.copy(role = "member", version = 2),
                            source.member.copy(role = "owner", version = 2),
                        ),
                    invitation = null,
                ),
            )
        runCurrent()
        assertFalse(model.state.value.canConfirm)
        model.onConfirm()
        runCurrent()
        assertTrue(source.deletions.isEmpty())
        assertNotNull(model.state.value.confirmation)
    }

    private fun withModel(
        block:
            suspend TestScope.(
                HouseholdDetailsScreenViewModel,
                FakeHouseholdManagementDataSource,
            ) -> Unit,
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FakeHouseholdManagementDataSource()
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
                    getHouseholdManagementStreamUseCase =
                        GetHouseholdManagementStreamUseCase(source),
                    replaceHouseholdInvitationUseCase = ReplaceHouseholdInvitationUseCase(source),
                    revokeHouseholdInvitationUseCase = RevokeHouseholdInvitationUseCase(source),
                    changeHouseholdMemberUseCase = ChangeHouseholdMemberUseCase(source),
                    deleteHouseholdUseCase = DeleteHouseholdUseCase(source),
                )
                .also { store.put("home", it) }
        try {
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.state.collect()
            }
            runCurrent()
            advanceTimeBy(300)
            runCurrent()
            block(model, source)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
