package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import kotlinx.coroutines.flow.StateFlow

class GetSettingsStreamUseCase(
    private val settingsDataSource: SettingsDataSource,
) {

    operator fun invoke(): StateFlow<SettingsDomain> = settingsDataSource.stream()
}
