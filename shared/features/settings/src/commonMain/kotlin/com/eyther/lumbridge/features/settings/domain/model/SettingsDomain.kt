package com.eyther.lumbridge.features.settings.domain.model

import com.eyther.lumbridge.core.domain.*

data class SettingsDomain(val theme: String = "SYSTEM", val notifications: Boolean = false)
