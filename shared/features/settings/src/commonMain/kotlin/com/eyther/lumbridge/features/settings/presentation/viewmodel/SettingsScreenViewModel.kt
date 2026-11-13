package com.eyther.lumbridge.features.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.ObserveSettingsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsScreenViewModelContract
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.SettingsState
import kotlinx.coroutines.flow.*

class SettingsScreenViewModel(
    observe: ObserveSettingsUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setNotificationsUseCase: SetNotificationsUseCase,
    private val getMembersUseCase: GetMembersUseCase,
    private val createInvitationUseCase: CreateInvitationUseCase,
    private val context: HouseholdContextDomain,
) : SettingsScreenViewModelContract() {
    private val mutableState = MutableStateFlow(SettingsState())
    override val state = mutableState.asStateFlow()

    init {
        mutableState.observeWith(viewModelScope) {
            observe().onEach { preferences ->
                mutableState.update { it.copy(preferences = preferences) }
            }
        }
        launch {
            when (val result = getMembersUseCase(context.id)) {
                is AppResult.Success -> mutableState.update { it.copy(members = result.value) }
                is AppResult.Failure -> mutableState.update { it.copy(error = result.message) }
            }
        }
    }

    override fun theme(value: String) {
        launch {
            val result = setThemeUseCase(value)
            if (result is AppResult.Failure) mutableState.update { it.copy(error = result.message) }
        }
    }

    override fun notifications(enabled: Boolean) {
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            val result = setNotificationsUseCase(enabled)
            mutableState.update {
                it.copy(busy = false, error = (result as? AppResult.Failure)?.message)
            }
        }
    }

    override fun invite() {
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = createInvitationUseCase(context.id)) {
                is AppResult.Success ->
                    mutableState.update { it.copy(busy = false, invitation = result.value) }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }
}
