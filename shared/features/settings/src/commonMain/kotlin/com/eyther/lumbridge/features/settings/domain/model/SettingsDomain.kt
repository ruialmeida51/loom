package com.eyther.lumbridge.features.settings.domain.model

import com.eyther.lumbridge.core.domain.AppLanguage

data class SettingsDomain(
    val theme: String = "SYSTEM",
    val language: AppLanguage = AppLanguage.SYSTEM,
    val notifications: Boolean = false,
    val notificationPrimeAnswered: Boolean = false,
)
