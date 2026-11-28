package com.eyther.lumbridge.features.home.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.features.home.domain.usecase.GetMemberProfileStreamUseCase
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.MemberProfileScreenViewModelContract
import com.eyther.lumbridge.features.home.presentation.viewmodel.contracts.MemberProfileState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class MemberProfileScreenViewModel(
    private val params: MemberProfileScreenViewModelContract.Params,
    private val getMemberProfileStreamUseCase: GetMemberProfileStreamUseCase,
    context: HouseholdContextDomain,
) : MemberProfileScreenViewModelContract() {
    private val refreshRequests = MutableStateFlow(0)
    private val mutableState =
        MutableStateFlow(
                MemberProfileState(
                    userId = context.userId,
                    currency = context.currency,
                    today = todayIso(context.timeZone),
                    timeZone = context.timeZone,
                ),
            )
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getMemberProfileStreamUseCase(memberId = params.memberId)
                            .delayFirstAtLeast(
                                enabled = state.value.loading || state.value.refreshing,
                            )
                    }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    profile = result.value,
                                    missing = result.value == null,
                                    loading = false,
                                    refreshing = false,
                                    loadError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    loading = false,
                                    refreshing = false,
                                    loadError = result.error.toTextResource(),
                                )
                        }
                    }
            }

    override val state = mutableState.asStateFlow()

    override fun onRetry() {
        if (state.value.refreshing) return
        mutableState.update { oldState -> oldState.copy(refreshing = true, loadError = null) }
        refreshRequests.update { oldState -> oldState + 1 }
    }
}
