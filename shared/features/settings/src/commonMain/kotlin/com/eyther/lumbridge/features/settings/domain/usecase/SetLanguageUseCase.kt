package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource

class SetLanguageUseCase(
    private val settingsDataSource: SettingsDataSource,
) {

    suspend operator fun invoke(language: AppLanguage): AppResult<Unit> =
        settingsDataSource.setLanguage(language)
}
