package com.eyther.lumbridge.features.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.usecase.GetUsageDataPreferencesStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.InitializeUsageDataConsentUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SaveUsageDataConsentUseCase
import com.eyther.lumbridge.features.settings.presentation.mapper.toUsageDataTextResource
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.UsageDataConsentScreenViewModelContract
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.UsageDataConsentState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

/**
 * Choices are drafts until saved. Closing the first prompt preserves any prior analytics choice.
 */
class UsageDataConsentScreenViewModel(
    private val getPreferences: GetUsageDataPreferencesStreamUseCase,
    private val initializeConsent: InitializeUsageDataConsentUseCase,
    private val saveConsent: SaveUsageDataConsentUseCase,
) : UsageDataConsentScreenViewModelContract() {
    private data class InnerInputState(
        val analytics: Boolean = false,
        val crashReports: Boolean = false,
    )

    private val inputState = MutableStateFlow(InnerInputState())
    private var startupRequested = false
    private var openRequested = false
    private val mutableState =
        MutableStateFlow(UsageDataConsentState())
            .launchWith(viewModelScope) { state ->
                val result = initializeConsent()
                state.update { oldState ->
                    oldState.copy(
                        error = (result as? AppResult.Failure)?.error?.toUsageDataTextResource(),
                    )
                }
            }
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(analytics = input.analytics, crashReports = input.crashReports)
                }
            }
            .observeWith(viewModelScope) { state ->
                getPreferences()
                    .onEach { preferences ->
                        if (
                            !state.value.visible &&
                                preferences.initialized &&
                                (openRequested || startupRequested && preferences.shouldAsk)
                        ) {
                            inputState.value =
                                InnerInputState(
                                    analytics = preferences.analyticsEnabled,
                                    crashReports = preferences.crashReportsEnabled,
                                )
                            openRequested = false
                            state.update { oldState -> oldState.copy(visible = true, error = null) }
                        }
                    }
                    .updateState(state) { preferences -> copy(preferences = preferences) }
            }
    override val state = mutableState.asStateFlow()

    override fun onStartup() {
        startupRequested = true
        if (state.value.preferences.shouldAsk) onOpen()
    }

    override fun onOpen() {
        if (state.value.busy) return
        val preferences = state.value.preferences
        if (!preferences.initialized) {
            openRequested = true
            if (state.value.error != null)
                mutableState.update { oldState -> oldState.copy(visible = true) }
            return
        }
        inputState.value =
            InnerInputState(
                analytics = preferences.analyticsEnabled,
                crashReports = preferences.crashReportsEnabled,
            )
        mutableState.update { oldState -> oldState.copy(visible = true, error = null) }
    }

    override fun onRetry() {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = initializeConsent()
            mutableState.update { oldState ->
                oldState.copy(
                    busy = false,
                    error = (result as? AppResult.Failure)?.error?.toUsageDataTextResource(),
                )
            }
        }
    }

    override fun onAnalyticsChanged(enabled: Boolean) {
        if (state.value.busy || !state.value.preferences.analyticsAvailable) return
        inputState.update { oldState -> oldState.copy(analytics = enabled) }
    }

    override fun onCrashReportsChanged(enabled: Boolean) {
        if (state.value.busy || !state.value.preferences.crashReportsAvailable) return
        inputState.update { oldState -> oldState.copy(crashReports = enabled) }
    }

    override fun onAllowAll() =
        save(
            analytics = state.value.preferences.analyticsAvailable,
            crashReports = state.value.preferences.crashReportsAvailable,
        )

    override fun onSave() =
        save(analytics = inputState.value.analytics, crashReports = inputState.value.crashReports)

    override fun onDismiss() {
        if (state.value.busy) return
        if (state.value.preferences.answered || !state.value.preferences.initialized) {
            openRequested = false
            startupRequested = false
            mutableState.update { oldState -> oldState.copy(visible = false) }
        } else
            save(
                analytics = state.value.preferences.analyticsEnabled,
                crashReports = state.value.preferences.crashReportsEnabled,
            )
    }

    private fun save(analytics: Boolean, crashReports: Boolean) {
        if (state.value.busy || !state.value.preferences.initialized) return
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            when (val result = saveConsent(analytics = analytics, crashReports = crashReports)) {
                is AppResult.Success -> {
                    startupRequested = false
                    openRequested = false
                    mutableState.update { oldState -> oldState.copy(busy = false, visible = false) }
                }
                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(busy = false, error = result.error.toUsageDataTextResource())
                    }
            }
        }
    }
}
