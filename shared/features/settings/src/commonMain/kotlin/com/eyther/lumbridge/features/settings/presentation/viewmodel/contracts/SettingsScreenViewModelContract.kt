package com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class SettingsScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<SettingsState>

    abstract fun onThemeSelected(value: String)

    abstract fun onNotificationsChanged(enabled: Boolean)

    abstract fun onAnalyticsConsentChanged(enabled: Boolean)

    abstract fun onCreateInvitation()

    abstract fun onDismissError()
}
