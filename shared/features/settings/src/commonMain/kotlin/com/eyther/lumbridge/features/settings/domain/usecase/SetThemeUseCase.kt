package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.errors.SettingsError

class SetThemeUseCase(
    private val settingsDataSource: SettingsDataSource,
) {

    suspend operator fun invoke(value: String): AppResult<Unit> {
        if (value !in listOf("SYSTEM", "LIGHT", "DARK"))
            return AppResult.Failure(SettingsError.InvalidTheme())
        return settingsDataSource.setTheme(value)
    }
}
