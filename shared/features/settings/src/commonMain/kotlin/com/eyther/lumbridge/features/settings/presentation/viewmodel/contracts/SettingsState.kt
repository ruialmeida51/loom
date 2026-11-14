package com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain

data class SettingsState(
    val userId: String = "",
    val nickname: String = "",
    val showNickname: Boolean = false,
    val nicknameError: TextResource? = null,
    val analytics: AnalyticsPreferencesDomain = AnalyticsPreferencesDomain(),
    val preferences: SettingsDomain = SettingsDomain(),
    val members: List<MemberDomain> = emptyList(),
    val invitation: String? = null,
    val showInvitation: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
) {
    val currentMember: MemberDomain? = members.firstOrNull { it.id == userId }
    val canManageHousehold: Boolean = currentMember?.role == "owner"
    val canEditNickname: Boolean = currentMember != null && !busy
    val canSaveNickname: Boolean =
        nickname.trim().isNotEmpty() && nickname != currentMember?.name && !busy
}
