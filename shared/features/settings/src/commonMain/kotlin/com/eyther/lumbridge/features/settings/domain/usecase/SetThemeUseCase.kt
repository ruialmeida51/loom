package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource

class SetThemeUseCase(private val source: SettingsDataSource) {
    suspend operator fun invoke(value: String): AppResult<Unit> {
        if (value !in listOf("SYSTEM", "LIGHT", "DARK")) return AppResult.Failure("Choose a theme.")
        return source.theme(value)
    }
}
