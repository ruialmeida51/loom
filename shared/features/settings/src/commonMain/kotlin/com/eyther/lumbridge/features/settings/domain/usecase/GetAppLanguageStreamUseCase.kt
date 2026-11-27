package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import kotlinx.coroutines.flow.StateFlow

class GetAppLanguageStreamUseCase(
    private val settingsDataSource: SettingsDataSource,
) {

    operator fun invoke(): StateFlow<AppLanguage> = settingsDataSource.streamLanguage()
}
