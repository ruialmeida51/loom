package com.eyther.lumbridge.features.household.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RevokeInvitationUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdDetailsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class HouseholdDetailsScreenViewModel(
    private val context: HouseholdContextDomain,
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
    private val revokeInvitationUseCase: RevokeInvitationUseCase,
    private val createInvitationUseCase: CreateInvitationUseCase,
) : HouseholdDetailsScreenViewModelContract() {
    private val refreshRequests = MutableStateFlow(0)
    private val mutableState =
        MutableStateFlow(HouseholdDetailsState(userId = context.userId)).observeWith(
            viewModelScope,
        ) { state ->
            refreshRequests
                .flatMapLatest {
                    getMembersStreamUseCase(householdId = context.id)
                        .delayFirstAtLeast(enabled = state.value.loading || state.value.refreshing)
                }
                .updateState(state) { result ->
                    when (result) {
                        is AppResult.Success ->
                            copy(
                                members = result.value,
                                loading = false,
                                refreshing = false,
                                loadError = null,
                            )
                        is AppResult.Failure ->
                            copy(
                                loading = false,
                                refreshing = false,
                                loadError = result.error.toHouseholdTextResource(),
                            )
                    }
                }
        }
    override val state = mutableState.asStateFlow()

    override fun onRefresh() {
        if (state.value.refreshing) return
        mutableState.update { oldState -> oldState.copy(refreshing = true, loadError = null) }
        refreshRequests.update { oldState -> oldState + 1 }
    }

    override fun onCreateInvitation() {
        if (!state.value.canInvite) return
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = createInvitationUseCase(id = context.id)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(busy = false, invitation = result.value)
                    is AppResult.Failure ->
                        oldState.copy(busy = false, error = result.error.toHouseholdTextResource())
                }
            }
        }
    }

    override fun onRevokeInvitation() {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = revokeInvitationUseCase(householdId = context.id)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(busy = false, invitation = null)
                    is AppResult.Failure ->
                        oldState.copy(busy = false, error = result.error.toHouseholdTextResource())
                }
            }
        }
    }

    override fun onCloseInvitation() {
        mutableState.update { oldState -> oldState.copy(invitation = null) }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null, loadError = null) }
    }
}
