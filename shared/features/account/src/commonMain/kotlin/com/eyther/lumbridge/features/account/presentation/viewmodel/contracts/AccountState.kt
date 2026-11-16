package com.eyther.lumbridge.features.account.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.account.domain.model.ProfileDomain
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain

data class AccountState(
    val profile: ProfileDomain? = null,
    val nickname: String = "",
    val showNickname: Boolean = false,
    val nicknameError: TextResource? = null,
    val preferences: SettingsDomain = SettingsDomain(),
    val analytics: AnalyticsPreferencesDomain = AnalyticsPreferencesDomain(),
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
) {
    val displayError: TextResource? = error ?: loadError
    val canEditNickname: Boolean = profile != null && !busy
    val canSaveNickname: Boolean =
        nickname.trim().isNotEmpty() && nickname.trim() != profile?.nickname && !busy
}
