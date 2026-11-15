package com.eyther.lumbridge.features.account.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import kotlinx.coroutines.flow.StateFlow

abstract class AccountScreenViewModelContract : LoomViewModel() {
    data class Params(val userId: String)

    abstract val state: StateFlow<AccountState>

    abstract fun onEditNickname()

    abstract fun onNicknameChanged(value: String)

    abstract fun onSaveNickname()

    abstract fun onCloseNickname()

    abstract fun onThemeSelected(value: String)

    abstract fun onNotificationsChanged(enabled: Boolean)

    abstract fun onAnalyticsConsentChanged(enabled: Boolean)

    abstract fun onRetry()

    abstract fun onDismissError()
}
