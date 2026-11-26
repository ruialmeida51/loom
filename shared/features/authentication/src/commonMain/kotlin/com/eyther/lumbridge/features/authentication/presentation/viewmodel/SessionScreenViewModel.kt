package com.eyther.lumbridge.features.authentication.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.FlowLaunchStrategy
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.components.snackbar.DEFAULT_ALERT_DURATION
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import com.eyther.lumbridge.features.authentication.domain.usecase.GetSessionStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RecoverSessionUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.authentication.generated.resources.Res
import com.eyther.lumbridge.features.authentication.generated.resources.household_exited
import com.eyther.lumbridge.features.authentication.presentation.mapper.toAuthenticationTextResource
import com.eyther.lumbridge.features.authentication.presentation.model.SessionStageUi
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionState
import com.eyther.lumbridge.features.household.domain.errors.HouseholdError
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Session identity, rather than lifecycle events or refreshed tokens, owns household restoration.
 */
/** Lets the SDK's process-lifecycle callback settle before checking its refresh job. */
private const val RESUME_RECOVERY_DELAY_MILLIS = 250L

/** A restore still pending after this long offers Retry instead of an indefinite spinner. */
private const val STALLED_STARTUP_DELAY_MILLIS = 15_000L

@OptIn(ExperimentalCoroutinesApi::class)
class SessionScreenViewModel(
    private val getSessionStreamUseCase: GetSessionStreamUseCase,
    private val recoverSessionUseCase: RecoverSessionUseCase,
    private val restoreHouseholdUseCase: RestoreHouseholdUseCase,
    private val rememberHouseholdUseCase: RememberHouseholdUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getHouseholdsStreamUseCase: GetHouseholdsStreamUseCase,
) : SessionScreenViewModelContract() {
    private var feedbackJob: Job? = null
    private var restoreJob: Job? = null
    private var selectionJob: Job? = null
    private val selectionMutex = Mutex()
    private var signOutJob: Job? = null
    private var sessionRecoveryJob: Job? = null
    private var resumeRecoveryJob: Job? = null
    private var identityRevision: Long = 0
    private val mutableState =
        MutableStateFlow(SessionState())
            .observeWith(
                scope = viewModelScope,
                launchStrategy = FlowLaunchStrategy.Eagerly,
            ) { state ->
                getSessionStreamUseCase().onEach { authentication ->
                    when (authentication) {
                        is AuthenticationStateDomain.Restoring ->
                            state.update { oldState ->
                                oldState.copy(
                                    reconnecting = authentication.reconnecting,
                                    sessionRecoveryAvailable =
                                        oldState.sessionRecoveryAvailable ||
                                            authentication.reconnecting,
                                )
                            }
                        AuthenticationStateDomain.SignedOut -> {
                            cancelIdentityWork()
                            state.value = SessionState(stage = SessionStageUi.SIGNED_OUT)
                        }
                        is AuthenticationStateDomain.Authenticated -> {
                            val userId = authentication.session.userId
                            if (state.value.userId == userId) {
                                state.update { oldState ->
                                    oldState.copy(
                                        reconnecting = false,
                                        sessionRecoveryAvailable = false,
                                        recoveringSession = false,
                                        sessionRecoveryError = null,
                                    )
                                }
                            } else {
                                cancelIdentityWork()
                                state.value = SessionState(userId = userId)
                                restoreHousehold(userId = userId, state = state)
                            }
                        }
                    }
                }
            }
            .observeWith(scope = viewModelScope, launchStrategy = FlowLaunchStrategy.Eagerly) {
                state ->
                state
                    .map { it.context }
                    .distinctUntilChanged()
                    .flatMapLatest { context ->
                        if (context == null) emptyFlow()
                        else
                            getHouseholdsStreamUseCase().onEach { result ->
                                if (
                                    state.value.context?.id == context.id &&
                                        state.value.userId == context.userId
                                ) {
                                    when (result) {
                                        is AppResult.Success -> {
                                            if (result.value.none { it.id == context.id }) {
                                                onHouseholdExited(
                                                    householdId = context.id,
                                                    explicit = false,
                                                )
                                            } else
                                                state.update { oldState ->
                                                    oldState.copy(accessError = null)
                                                }
                                        }
                                        is AppResult.Failure ->
                                            state.update { oldState ->
                                                oldState.copy(
                                                    accessError =
                                                        result.error.toHouseholdTextResource(),
                                                )
                                            }
                                    }
                                }
                            }
                    }
            }
    override val state = mutableState.asStateFlow()

    override fun onResume() {
        if (state.value.stage != SessionStageUi.READY) return
        resumeRecoveryJob?.cancel()
        val revision = identityRevision
        resumeRecoveryJob = launch {
            // Let the SDK's process callback finish before checking for a stopped refresh job.
            delay(RESUME_RECOVERY_DELAY_MILLIS)
            // Best effort: offline failures stay silent while SDK/read-stream retries continue,
            // and a confirmed revocation still arrives through the session stream.
            if (revision == identityRevision && state.value.stage == SessionStageUi.READY)
                recoverSessionUseCase()
        }
    }

    override fun onPause() {
        resumeRecoveryJob?.cancel()
    }

    init {
        launch {
            delay(STALLED_STARTUP_DELAY_MILLIS)
            mutableState.update { oldState ->
                if (oldState.userId == null && oldState.stage == SessionStageUi.RESTORING)
                    oldState.copy(reconnecting = true, sessionRecoveryAvailable = true)
                else oldState
            }
        }
    }

    override fun onRetrySessionRecovery() {
        if (state.value.stage != SessionStageUi.RESTORING || sessionRecoveryJob?.isActive == true)
            return
        mutableState.update { oldState ->
            oldState.copy(recoveringSession = true, sessionRecoveryError = null)
        }
        val revision = identityRevision
        sessionRecoveryJob = launch {
            val result = recoverSessionUseCase()
            ensureActive()
            if (revision != identityRevision || state.value.stage != SessionStageUi.RESTORING)
                return@launch
            mutableState.update { oldState ->
                oldState.copy(
                    recoveringSession = false,
                    sessionRecoveryAvailable = true,
                    sessionRecoveryError =
                        (result as? AppResult.Failure)?.error?.toAuthenticationTextResource(),
                )
            }
        }
    }

    override fun onHouseholdSelected(household: HouseholdDomain) {
        val userId = state.value.userId ?: return
        if (state.value.stage != SessionStageUi.READY || state.value.signingOut) return
        restoreJob?.cancel()
        val revision = identityRevision
        selectionJob?.cancel()
        mutableState.update { oldState ->
            oldState.copy(
                context = household.toContext(userId = userId),
                accessError = null,
                selectionError = null,
                selectionMessage = null,
            )
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

    override fun onHouseholdExited(householdId: String, explicit: Boolean) {
        val userId = state.value.userId ?: return
        if (state.value.context?.id != householdId || state.value.signingOut) return
        restoreJob?.cancel()
        selectionJob?.cancel()
        val revision = identityRevision
        mutableState.update { oldState ->
            oldState.copy(
                context = null,
                accessError = null,
                stage = SessionStageUi.READY,
                selectionError =
                    if (explicit) null else HouseholdError.AccessDenied().toHouseholdTextResource(),
                selectionMessage =
                    if (explicit) TextResource.Resource(Res.string.household_exited) else null,
            )
        }
        selectionJob = launch {
            selectionMutex.withLock {
                ensureActive()
                val result = rememberHouseholdUseCase(userId = userId, householdId = null)
                ensureActive()
                if (revision == identityRevision && result is AppResult.Failure)
                    mutableState.update { oldState ->
                        oldState.copy(selectionError = result.error.toHouseholdTextResource())
                    }
            }
        }
        feedbackJob?.cancel()
        feedbackJob = launch {
            delay(DEFAULT_ALERT_DURATION)
            mutableState.update { oldState -> oldState.copy(selectionMessage = null) }
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
        feedbackJob?.cancel()
        resumeRecoveryJob?.cancel()
        sessionRecoveryJob?.cancel()
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
