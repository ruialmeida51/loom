package com.eyther.lumbridge.features.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.analytics.domain.usecase.GetAnalyticsPreferencesStreamUseCase
import com.eyther.lumbridge.features.analytics.domain.usecase.SetAnalyticsConsentUseCase
import com.eyther.lumbridge.features.analytics.presentation.mapper.toAnalyticsTextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.UpdateMemberNicknameUseCase
import com.eyther.lumbridge.features.household.presentation.mapper.toHouseholdTextResource
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import com.eyther.lumbridge.features.settings.presentation.mapper.toSettingsTextResource
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsScreenViewModel(
    private val updateMemberNicknameUseCase: UpdateMemberNicknameUseCase,
    private val getSettingsStreamUseCase: GetSettingsStreamUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setNotificationsUseCase: SetNotificationsUseCase,
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
    private val createInvitationUseCase: CreateInvitationUseCase,
    private val context: HouseholdContextDomain,
    private val getAnalyticsPreferencesStreamUseCase: GetAnalyticsPreferencesStreamUseCase,
    private val setAnalyticsConsentUseCase: SetAnalyticsConsentUseCase,
) : SettingsScreenViewModelContract() {

    private data class InnerInputState(val nickname: String = "")

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(SettingsState(userId = context.userId))
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input -> copy(nickname = input.nickname) }
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
            .observeWith(viewModelScope) { state ->
                getMembersStreamUseCase(householdId = context.id).updateState(state) { result ->
                    when (result) {
                        is AppResult.Success -> copy(members = result.value)
                        is AppResult.Failure -> copy(error = result.error.toHouseholdTextResource())
                    }
                }
            }

    override val state = mutableState.asStateFlow()

    override fun onEditNickname() {
        if (state.value.busy) return
        inputState.value = InnerInputState(nickname = state.value.currentMember?.name.orEmpty())
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
        if (state.value.busy) return
        val name = inputState.value.nickname.trim()
        mutableState.update { oldState -> oldState.copy(busy = true, nicknameError = null) }
        launch {
            val result = updateMemberNicknameUseCase(userId = context.userId, nickname = name)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            busy = false,
                            showNickname = false,
                            members =
                                oldState.members.map { member ->
                                    if (member.id == context.userId) member.copy(name = name)
                                    else member
                                },
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            nicknameError = result.error.toHouseholdTextResource(),
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

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = setNotificationsUseCase(enabled)

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
        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }
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

    override fun onCreateInvitation() {
        if (state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = createInvitationUseCase(context.id)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            busy = false,
                            invitation = result.value,
                            showInvitation = true,
                        )

                    is AppResult.Failure ->
                        oldState.copy(
                            busy = false,
                            error = result.error.toHouseholdTextResource(),
                        )
                }
            }
        }
    }

    override fun onCloseInvitation() {
        mutableState.update { oldState -> oldState.copy(showInvitation = false) }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }
}
