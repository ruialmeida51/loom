package com.eyther.lumbridge.features.account.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.account.domain.usecase.GetProfileStreamUseCase
import com.eyther.lumbridge.features.account.domain.usecase.UpdateNicknameUseCase
import com.eyther.lumbridge.features.account.presentation.mapper.toAccountTextResource
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountScreenViewModelContract
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountState
import com.eyther.lumbridge.features.analytics.domain.usecase.GetAnalyticsPreferencesStreamUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.SetAnalyticsConsentUseCase
import com.eyther.lumbridge.features.analytics.presentation.mapper.toAnalyticsTextResource
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import com.eyther.lumbridge.features.settings.presentation.mapper.toSettingsTextResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class AccountScreenViewModel(
    private val params: AccountScreenViewModelContract.Params,
    private val getProfileStreamUseCase: GetProfileStreamUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    private val getSettingsStreamUseCase: GetSettingsStreamUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setNotificationsUseCase: SetNotificationsUseCase,
    private val getAnalyticsPreferencesStreamUseCase: GetAnalyticsPreferencesStreamUseCase,
    private val setAnalyticsConsentUseCase: SetAnalyticsConsentUseCase,
) : AccountScreenViewModelContract() {
    private data class InnerInputState(val nickname: String = "")

    private val inputState = MutableStateFlow(InnerInputState())
    private val refreshRequests = MutableStateFlow(0)
    private val mutableState =
        MutableStateFlow(AccountState())
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input -> copy(nickname = input.nickname) }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest { getProfileStreamUseCase(userId = params.userId) }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success ->
                                copy(profile = result.value, loading = false, error = null)
                            is AppResult.Failure ->
                                copy(loading = false, error = result.error.toAccountTextResource())
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                getSettingsStreamUseCase().updateState(state) { preferences ->
                    copy(preferences = preferences)
                }
            }
            .observeWith(viewModelScope) { state ->
                getAnalyticsPreferencesStreamUseCase().updateState(state) { analytics ->
                    copy(analytics = analytics)
                }
            }

    override val state = mutableState.asStateFlow()

    override fun onEditNickname() {
        if (!state.value.canEditNickname) return
        inputState.value = InnerInputState(nickname = state.value.profile?.nickname.orEmpty())
        mutableState.update { oldState -> oldState.copy(showNickname = true, nicknameError = null) }
    }

    override fun onNicknameChanged(value: String) {
        if (state.value.busy) return
        inputState.update { oldState -> oldState.copy(nickname = value) }
        mutableState.update { oldState -> oldState.copy(nicknameError = null) }
    }

    override fun onCloseNickname() {
        if (state.value.busy) return
        mutableState.update { oldState ->
            oldState.copy(showNickname = false, nicknameError = null)
        }
    }

    override fun onSaveNickname() {
        if (!state.value.canSaveNickname) return
        val nickname = inputState.value.nickname
        mutableState.update { oldState -> oldState.copy(busy = true, nicknameError = null) }
        launch {
            val result = updateNicknameUseCase(userId = params.userId, nickname = nickname)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(profile = result.value, busy = false, showNickname = false)
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            nicknameError = result.error.toAccountTextResource(),
                        )
                }
            }
        }
    }

    override fun onThemeSelected(value: String) {
        launch {
            val result = setThemeUseCase(value)
            mutableState.update { oldState ->
                oldState.copy(
                    error = (result as? AppResult.Failure)?.error?.toSettingsTextResource(),
                )
            }
        }
    }

    override fun onNotificationsChanged(enabled: Boolean) {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = setNotificationsUseCase(enabled = enabled)
            mutableState.update { oldState ->
                oldState.copy(
                    busy = false,
                    error = (result as? AppResult.Failure)?.error?.toSettingsTextResource(),
                )
            }
        }
    }

    override fun onAnalyticsConsentChanged(enabled: Boolean) {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val result = setAnalyticsConsentUseCase(enabled = enabled)
            mutableState.update { oldState ->
                oldState.copy(
                    busy = false,
                    error = (result as? AppResult.Failure)?.error?.toAnalyticsTextResource(),
                )
            }
        }
    }

    override fun onRetry() {
        refreshRequests.update { oldState -> oldState + 1 }
    }

    override fun onDismissError() {
        mutableState.update { oldState -> oldState.copy(error = null) }
    }
}
