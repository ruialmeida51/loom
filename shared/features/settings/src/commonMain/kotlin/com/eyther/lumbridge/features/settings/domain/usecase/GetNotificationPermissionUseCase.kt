package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain

class GetNotificationPermissionUseCase(private val settingsDataSource: SettingsDataSource) {
    suspend operator fun invoke(): AppResult<NotificationPermissionDomain> =
        settingsDataSource.getNotificationPermission()
}
