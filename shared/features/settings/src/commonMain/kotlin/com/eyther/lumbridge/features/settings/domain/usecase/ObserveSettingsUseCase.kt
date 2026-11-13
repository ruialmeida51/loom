package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource

class ObserveSettingsUseCase(private val source: SettingsDataSource) {
    operator fun invoke() = source.preferences
}
