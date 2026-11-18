package com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class UsageDataConsentScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<UsageDataConsentState>

    abstract fun onStartup()

    abstract fun onRetry()

    abstract fun onOpen()

    abstract fun onAnalyticsChanged(enabled: Boolean)

    abstract fun onCrashReportsChanged(enabled: Boolean)

    abstract fun onAllowAll()

    abstract fun onSave()

    abstract fun onDismiss()
}
