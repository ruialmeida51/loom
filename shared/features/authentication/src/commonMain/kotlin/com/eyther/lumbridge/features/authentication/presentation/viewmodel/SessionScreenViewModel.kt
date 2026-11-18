package com.eyther.lumbridge.features.authentication.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.FlowLaunchStrategy
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import com.eyther.lumbridge.features.authentication.domain.usecase.GetSessionStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.authentication.presentation.mapper.toAuthenticationTextResource
import com.eyther.lumbridge.features.authentication.presentation.model.SessionStageUi
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionState
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Session identity, rather than lifecycle events or refreshed tokens, owns household restoration.
 */
class SessionScreenViewModel(
    private val getSessionStreamUseCase: GetSessionStreamUseCase,
    private val restoreHouseholdUseCase: RestoreHouseholdUseCase,
    private val rememberHouseholdUseCase: RememberHouseholdUseCase,
    private val signOutUseCase: SignOutUseCase,
) : SessionScreenViewModelContract() {
    private var restoreJob: Job? = null
    private var selectionJob: Job? = null
    private val selectionMutex = Mutex()
    private var signOutJob: Job? = null
    private var identityRevision: Long = 0
    private val mutableState =
        MutableStateFlow(SessionState()).observeWith(
            scope = viewModelScope,
            launchStrategy = FlowLaunchStrategy.Eagerly,
        ) { state ->
            getSessionStreamUseCase().onEach { authentication ->
                when (authentication) {
                    is AuthenticationStateDomain.Restoring ->
                        state.update { oldState ->
                            oldState.copy(reconnecting = authentication.reconnecting)
                        }
                    AuthenticationStateDomain.SignedOut -> {
                        cancelIdentityWork()
                        state.value = SessionState(stage = SessionStageUi.SIGNED_OUT)
                    }
                    is AuthenticationStateDomain.Authenticated -> {
                        val userId = authentication.session.userId
                        if (state.value.userId == userId) {
                            state.update { oldState -> oldState.copy(reconnecting = false) }
                        } else {
                            cancelIdentityWork()
                            state.value = SessionState(userId = userId)
                            restoreHousehold(userId = userId, state = state)
                        }
                    }
                }
            }
        }
    override val state = mutableState.asStateFlow()

    override fun onHouseholdSelected(household: HouseholdDomain) {
        val userId = state.value.userId ?: return
        if (state.value.stage != SessionStageUi.READY || state.value.signingOut) return
        restoreJob?.cancel()
        val revision = identityRevision
        selectionJob?.cancel()
        mutableState.update { oldState ->
            oldState.copy(context = household.toContext(userId = userId), selectionError = null)
        }
        selectionJob = launch {
            // A canceled device write may finish in I/O; the next write must wait for its lock.
            selectionMutex.withLock {
                ensureActive()
                val result = rememberHouseholdUseCase(userId = userId, householdId = household.id)
                ensureActive()
                if (revision == identityRevision) {
                    mutableState.update { oldState ->
                        oldState.copy(
                            selectionError =
                                (result as? AppResult.Failure)?.error?.toHouseholdTextResource(),
                        )
                    }
                }
            }
        }
    }

    override fun onRetryHouseholdRestore() {
        val userId = state.value.userId ?: return
        if (state.value.stage != SessionStageUi.HOUSEHOLD_RESTORE_FAILED) return
        mutableState.update { oldState ->
            oldState.copy(stage = SessionStageUi.RESTORING, restoreError = null)
        }
        restoreHousehold(userId = userId, state = mutableState)
    }

    override fun onChooseHousehold() {
        if (state.value.stage != SessionStageUi.HOUSEHOLD_RESTORE_FAILED) return
        mutableState.update { oldState ->
            oldState.copy(stage = SessionStageUi.READY, restoreError = null)
        }
    }

    override fun onDismissSelectionError() {
        mutableState.update { oldState -> oldState.copy(selectionError = null) }
    }

    override fun onSignOutDismissed() {
        if (state.value.signingOut) return
        mutableState.update { oldState -> oldState.copy(signOutError = null) }
    }

    override fun onSignOut() {
        if (state.value.stage != SessionStageUi.READY || state.value.signingOut) return
        val revision = identityRevision
        mutableState.update { oldState -> oldState.copy(signingOut = true, signOutError = null) }
        signOutJob = launch {
            val result = signOutUseCase()
            if (revision != identityRevision) return@launch
            mutableState.update { oldState ->
                oldState.copy(
                    signingOut = false,
                    signOutError =
                        (result as? AppResult.Failure)?.error?.toAuthenticationTextResource(),
                )
            }
        }
    }

    private fun restoreHousehold(userId: String, state: MutableStateFlow<SessionState>) {
        restoreJob?.cancel()
        val revision = identityRevision
        restoreJob = launch {
            val result = restoreHouseholdUseCase(userId = userId)
            ensureActive()
            if (revision != identityRevision) return@launch
            state.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            stage = SessionStageUi.READY,
                            context = result.value?.toContext(userId = userId),
                            restoreError = null,
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            stage = SessionStageUi.HOUSEHOLD_RESTORE_FAILED,
                            restoreError = result.error.toHouseholdTextResource(),
                        )
                }
            }
        }
    }

    private fun cancelIdentityWork() {
        identityRevision++
        restoreJob?.cancel()
        selectionJob?.cancel()
        signOutJob?.cancel()
    }

    private fun HouseholdDomain.toContext(userId: String): HouseholdContextDomain =
        HouseholdContextDomain(
            id = id,
            userId = userId,
            name = name,
            currency = currency,
            timeZone = timeZone,
        )
}
