package com.eyther.lumbridge.features.household.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.errors.PictureError
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.mapToResult
import com.eyther.lumbridge.core.presentation.components.snackbar.DEFAULT_ALERT_DURATION
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdInvitationDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdMemberActionDomain
import com.eyther.lumbridge.features.household.domain.usecase.ChangeHouseholdMemberUseCase
import com.eyther.lumbridge.features.household.domain.usecase.DeleteHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdManagementStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.ReplaceHouseholdInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RevokeHouseholdInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.SetHouseholdPictureUseCase
import com.eyther.lumbridge.features.household.generated.resources.Res
import com.eyther.lumbridge.features.household.generated.resources.invitation_copied
import com.eyther.lumbridge.features.household.generated.resources.management_updated
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import com.eyther.lumbridge.features.household.presentation.model.HouseholdManagementActionUi
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdDetailsScreenViewModel(
    private val context: HouseholdContextDomain,
    private val getHouseholdManagementStreamUseCase: GetHouseholdManagementStreamUseCase,
    private val replaceHouseholdInvitationUseCase: ReplaceHouseholdInvitationUseCase,
    private val revokeHouseholdInvitationUseCase: RevokeHouseholdInvitationUseCase,
    private val changeHouseholdMemberUseCase: ChangeHouseholdMemberUseCase,
    private val deleteHouseholdUseCase: DeleteHouseholdUseCase,
    private val getHouseholdsStreamUseCase: GetHouseholdsStreamUseCase,
    private val setHouseholdPictureUseCase: SetHouseholdPictureUseCase,
) : HouseholdDetailsScreenViewModelContract() {
    private data class InnerInputState(val confirmationName: String = "")

    private val inputState = MutableStateFlow(InnerInputState())
    private val refreshRequests = MutableStateFlow(0)
    private var feedbackJob: Job? = null
    private var pendingCreation: HouseholdManagementActionUi.InvitationChange? = null
    private val mutableState =
        MutableStateFlow(
                HouseholdDetailsState(userId = context.userId, householdName = context.name),
            )
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(confirmationName = input.confirmationName)
                }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest { requestVersion ->
                        getHouseholdManagementStreamUseCase()
                            .delayFirstAtLeast(
                                enabled = state.value.loading || state.value.refreshing,
                            )
                            .filter { requestVersion == refreshRequests.value && !state.value.busy }
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    members = result.value.members,
                                    invitation = result.value.invitation,
                                    loading = false,
                                    refreshing = false,
                                    loadError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    loading = false,
                                    refreshing = false,
                                    loadError = result.error.toHouseholdTextResource(),
                                    exited = exited || result.error is HouseholdError.AccessDenied,
                                )
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                getHouseholdsStreamUseCase().updateState(state) { result ->
                    when (result) {
                        is AppResult.Success ->
                            copy(picturePath = result.value.firstOrNull { it.id == context.id }?.imagePath)
                        // The picture is decoration; the badge falls back to the initial.
                        is AppResult.Failure -> this
                    }
                }
            }
    override val state = mutableState.asStateFlow()

    override fun onPicturePicked(bytes: ByteArray) {
        savePicture(bytes = bytes)
    }

    override fun onRemovePicture() {
        savePicture(bytes = null)
    }

    override fun onPictureError(error: PictureError) {
        mutableState.update { oldState -> oldState.copy(error = error.toHouseholdTextResource()) }
    }

    private fun savePicture(bytes: ByteArray?) {
        if (!state.value.canChangePicture) return
        mutableState.update { oldState -> oldState.copy(savingPicture = true, error = null) }
        launch {
            val result = setHouseholdPictureUseCase(householdId = context.id, bytes = bytes)
            mutableState.update { oldState ->
                oldState.copy(
                    savingPicture = false,
                    error = (result as? AppResult.Failure)?.error?.toHouseholdTextResource(),
                )
            }
        }
    }

    override fun onRefresh() {
        if (state.value.refreshing || state.value.busy) return
        mutableState.update { oldState -> oldState.copy(refreshing = true, loadError = null) }
        refreshRequests.update { oldVersion -> oldVersion + 1 }
    }

    override fun onCreateInvitation() {
        if (!state.value.canInvite) return
        val current = state.value.invitation
        if (current != null)
            showConfirmation(
                HouseholdManagementActionUi.InvitationChange(
                    current,
                    revoke = false,
                    requestId = newId(),
                ),
            )
        else {
            val target =
                pendingCreation
                    ?: HouseholdManagementActionUi.InvitationChange(
                        null,
                        revoke = false,
                        requestId = newId(),
                    )
            pendingCreation = target
            execute(target = target)
        }
    }

    override fun onRevokeInvitation() {
        if (!state.value.canInvite) return
        val invitation = state.value.invitation ?: return
        showConfirmation(
            HouseholdManagementActionUi.InvitationChange(
                invitation,
                revoke = true,
                requestId = newId(),
            ),
        )
    }

    override fun onMemberAction(memberId: String, action: HouseholdMemberActionDomain) {
        if (
            state.value.busy ||
                !state.value.isOwner ||
                memberId == context.userId ||
                action == HouseholdMemberActionDomain.LEAVE
        )
            return
        val member = state.value.members.firstOrNull { it.id == memberId } ?: return
        showConfirmation(
            HouseholdManagementActionUi.MemberChange(
                member = member,
                action = action,
                requestId = newId(),
            ),
        )
    }

    override fun onLeaveRequested() {
        if (state.value.busy) return
        val member = state.value.members.firstOrNull { it.id == context.userId } ?: return
        if (!state.value.canLeave) {
            mutableState.update { oldState ->
                oldState.copy(error = HouseholdError.LastOwner().toHouseholdTextResource())
            }
            return
        }
        showConfirmation(
            HouseholdManagementActionUi.MemberChange(
                member = member,
                action = HouseholdMemberActionDomain.LEAVE,
                requestId = newId(),
            ),
        )
    }

    override fun onDeleteRequested() {
        if (state.value.busy || !state.value.isOwner) return
        showConfirmation(HouseholdManagementActionUi.Delete(requestId = newId()))
    }

    override fun onConfirmationNameChanged(value: String) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(confirmationName = value) }
    }

    override fun onConfirm() {
        if (!state.value.canConfirm) return
        execute(target = state.value.confirmation ?: return)
    }

    override fun onConfirmationDismissed() {
        if (state.value.busy) return
        inputState.value = InnerInputState()
        mutableState.update { oldState -> oldState.copy(confirmation = null, error = null) }
    }

    override fun onCopiedInvitation() {
        showFeedback(message = TextResource.Resource(Res.string.invitation_copied))
    }

    override fun onShareUnavailable() {
        mutableState.update { oldState ->
            oldState.copy(error = HouseholdError.ShareUnavailable().toHouseholdTextResource())
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null, loadError = null) }
    }

    private fun showConfirmation(target: HouseholdManagementActionUi) {
        inputState.value = InnerInputState()
        mutableState.update { oldState -> oldState.copy(confirmation = target, error = null) }
    }

    private fun execute(target: HouseholdManagementActionUi) {
        if (state.value.busy) return
        val name = inputState.value.confirmationName
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result: AppResult<HouseholdInvitationDomain?> =
                when (target) {
                    is HouseholdManagementActionUi.InvitationChange ->
                        if (target.revoke) {
                            val invitation = target.invitation ?: return@launch
                            revokeHouseholdInvitationUseCase(
                                    invitation = invitation,
                                    requestId = target.requestId,
                                )
                                .mapToResult { null }
                        } else
                            replaceHouseholdInvitationUseCase(
                                    previous = target.invitation,
                                    requestId = target.requestId,
                                )
                                .mapToResult { it }
                    is HouseholdManagementActionUi.MemberChange ->
                        changeHouseholdMemberUseCase(
                                member = target.member,
                                action = target.action,
                                requestId = target.requestId,
                            )
                            .mapToResult { null }
                    is HouseholdManagementActionUi.Delete ->
                        deleteHouseholdUseCase(name = name, requestId = target.requestId)
                            .mapToResult { null }
                }
            refreshRequests.update { oldVersion -> oldVersion + 1 }
            when (result) {
                is AppResult.Success -> {
                    pendingCreation = null
                    val exited =
                        target is HouseholdManagementActionUi.Delete ||
                            (target is HouseholdManagementActionUi.MemberChange &&
                                target.action == HouseholdMemberActionDomain.LEAVE)
                    inputState.value = InnerInputState()
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = false,
                            confirmation = null,
                            error = null,
                            exited = exited,
                            exitExplicit = exited,
                            invitation =
                                if (target is HouseholdManagementActionUi.InvitationChange)
                                    result.value
                                else oldState.invitation,
                        )
                    }
                    if (!exited) {
                        showFeedback(message = TextResource.Resource(Res.string.management_updated))
                    }
                }
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(busy = false, error = result.error.toHouseholdTextResource())
                    }
            }
        }
    }

    private fun showFeedback(message: TextResource) {
        feedbackJob?.cancel()
        mutableState.update { oldState -> oldState.copy(successMessage = message) }
        feedbackJob = launch {
            delay(DEFAULT_ALERT_DURATION)
            mutableState.update { oldState -> oldState.copy(successMessage = null) }
        }
    }
}
