package com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.settings.domain.model.UsageDataPreferencesDomain

data class UsageDataConsentState(
    val preferences: UsageDataPreferencesDomain = UsageDataPreferencesDomain(),
    val visible: Boolean = false,
    val analytics: Boolean = false,
    val crashReports: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
)
