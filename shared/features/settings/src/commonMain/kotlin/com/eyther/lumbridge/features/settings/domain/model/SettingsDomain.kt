package com.eyther.lumbridge.features.settings.domain.model

data class SettingsDomain(
    val theme: String = "SYSTEM",
    val notifications: Boolean = false,
    val notificationPrimeAnswered: Boolean = false,
)
