package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource

class SetNotificationsUseCase(
    private val settingsDataSource: SettingsDataSource,
) {

    suspend operator fun invoke(enabled: Boolean): AppResult<Unit> = settingsDataSource.setNotifications(enabled)
}
