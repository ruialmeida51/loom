package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource

class MarkNotificationPrimeAnsweredUseCase(private val settingsDataSource: SettingsDataSource) {
    suspend operator fun invoke(): AppResult<Unit> =
        settingsDataSource.markNotificationPrimeAnswered()
}
