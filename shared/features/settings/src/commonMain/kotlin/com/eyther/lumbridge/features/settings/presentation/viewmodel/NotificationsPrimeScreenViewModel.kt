package com.eyther.lumbridge.features.settings.presentation.viewmodel

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.errors.SettingsError
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain
import com.eyther.lumbridge.features.settings.domain.usecase.GetNotificationPermissionUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.MarkNotificationPrimeAnsweredUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.OpenNotificationSettingsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.presentation.mapper.toSettingsTextResource
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.NotificationsPrimeScreenViewModelContract
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.NotificationsPrimeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Context can show an explanation; only an explicit Enable action reaches the system prompt. */
class NotificationsPrimeScreenViewModel(
    private val getSettings: GetSettingsStreamUseCase,
    private val getPermission: GetNotificationPermissionUseCase,
    private val markAnswered: MarkNotificationPrimeAnsweredUseCase,
    private val openSettings: OpenNotificationSettingsUseCase,
    private val setNotifications: SetNotificationsUseCase,
) : NotificationsPrimeScreenViewModelContract() {
    private val mutableState = MutableStateFlow(NotificationsPrimeState())
    override val state = mutableState.asStateFlow()
    private var checking = false

    override fun onContextRequested() = open(explicit = false)

    override fun onOpen() = open(explicit = true)

    private fun open(explicit: Boolean) {
        val preferences = getSettings().value
        if (checking || state.value.visible || state.value.busy) return
        if (!explicit && (preferences.notifications || preferences.notificationPrimeAnswered))
            return
        checking = true
        launch {
            when (val result = getPermission()) {
                is AppResult.Success -> {
                    val permission = result.value
                    val shouldShow =
                        explicit || permission == NotificationPermissionDomain.NOT_REQUESTED
                    if (shouldShow)
                        mutableState.update { oldState ->
                            oldState.copy(
                                visible = true,
                                permission = permission,
                                error = null,
                            )
                        }
                }
                is AppResult.Failure ->
                    if (explicit)
                        mutableState.update { oldState ->
                            oldState.copy(
                                visible = true,
                                error = result.error.toSettingsTextResource(),
                            )
                        }
            }
            checking = false
        }
    }

    override fun onEnable() {
        if (state.value.busy) return
        if (state.value.permission == NotificationPermissionDomain.DENIED) {
            mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
            launch {
                val result = openSettings()
                mutableState.update { oldState ->
                    oldState.copy(
                        busy = false,
                        waitingForSettings = result is AppResult.Success,
                        error = (result as? AppResult.Failure)?.error?.toSettingsTextResource(),
                    )
                }
            }
        } else register()
    }

    private fun register() {
        mutableState.update { oldState -> oldState.copy(busy = true, error = null) }
        launch {
            val registrationResult = setNotifications(enabled = true)
            val result =
                if (registrationResult is AppResult.Success) markAnswered() else registrationResult
            val permission = getPermission()
            val error = (result as? AppResult.Failure)?.error
            mutableState.update { oldState ->
                oldState.copy(
                    busy = false,
                    visible = result !is AppResult.Success,
                    waitingForSettings = false,
                    permission = (permission as? AppResult.Success)?.value ?: oldState.permission,
                    // A refusal is explained by the sheet itself, which now offers settings.
                    error = error?.takeUnless { it is SettingsError.PermissionDenied }
                        ?.toSettingsTextResource(),
                )
            }
        }
    }

    override fun onNotNow() {
        if (state.value.busy) return
        mutableState.update { oldState -> oldState.copy(busy = true) }
        launch {
            val result = markAnswered()
            mutableState.update { oldState ->
                oldState.copy(
                    busy = false,
                    visible = result !is AppResult.Success,
                    waitingForSettings = false,
                    error = (result as? AppResult.Failure)?.error?.toSettingsTextResource(),
                )
            }
        }
    }

    override fun onResume() {
        if (state.value.busy || checking) return
        checking = true
        launch {
            val result = getPermission()
            checking = false
            // Keep the phone's current answer, so My account can show when it blocks reminders.
            if (result is AppResult.Success)
                mutableState.update { oldState -> oldState.copy(permission = result.value) }
            if (!state.value.waitingForSettings && !getSettings().value.notifications) return@launch
            if (
                result is AppResult.Success &&
                    result.value == NotificationPermissionDomain.GRANTED &&
                    state.value.waitingForSettings
            )
                register()
            else if (result is AppResult.Failure && state.value.visible)
                mutableState.update { oldState ->
                    oldState.copy(error = result.error.toSettingsTextResource())
                }
        }
    }
}
