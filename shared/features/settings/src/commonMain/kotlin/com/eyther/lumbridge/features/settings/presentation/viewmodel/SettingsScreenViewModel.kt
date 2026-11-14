package com.eyther.lumbridge.features.settings.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.launchWith
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.CreateInvitationUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersUseCase
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
    private val getSettingsStreamUseCase: GetSettingsStreamUseCase,
    private val setThemeUseCase: SetThemeUseCase,
    private val setNotificationsUseCase: SetNotificationsUseCase,
    private val getMembersUseCase: GetMembersUseCase,
    private val createInvitationUseCase: CreateInvitationUseCase,
    private val context: HouseholdContextDomain,
) : SettingsScreenViewModelContract() {

    private val mutableState = MutableStateFlow(SettingsState())
        .observeWith(viewModelScope) { state ->
            getSettingsStreamUseCase().updateState(state) { preferences ->
                copy(preferences = preferences)
            }
        }
        .launchWith(viewModelScope) { state ->
            state.loadMembers()
        }

    override val state = mutableState.asStateFlow()

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

    override fun onCreateInvitation() {
        if (state.value.busy) return

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            val result = createInvitationUseCase(context.id)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        busy = false,
                        invitation = result.value,
                    )

                    is AppResult.Failure -> oldState.copy(
                        busy = false,
                        error = result.error.toHouseholdTextResource(),
                    )
                }
            }
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null)
        }
    }

    private suspend fun MutableStateFlow<SettingsState>.loadMembers() {
        val result = getMembersUseCase(context.id)

        update { oldState ->
            when (result) {
                is AppResult.Success -> oldState.copy(members = result.value)
                is AppResult.Failure -> oldState.copy(error = result.error.toHouseholdTextResource())
            }
        }
    }
}
