package com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class NotificationsPrimeScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<NotificationsPrimeState>

    abstract fun onContextRequested()

    abstract fun onOpen()

    abstract fun onEnable()

    abstract fun onNotNow()

    abstract fun onResume()
}
