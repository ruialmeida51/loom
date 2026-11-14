package com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.analytics.domain.model.AnalyticsPreferencesDomain
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain

data class SettingsState(
    val analytics: AnalyticsPreferencesDomain = AnalyticsPreferencesDomain(),
    val preferences: SettingsDomain = SettingsDomain(),
    val members: List<MemberDomain> = emptyList(),
    val invitation: String? = null,
    val busy: Boolean = false,
    val error: TextResource? = null,
)
