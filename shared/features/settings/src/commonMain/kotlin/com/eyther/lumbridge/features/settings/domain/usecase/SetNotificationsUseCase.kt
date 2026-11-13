package com.eyther.lumbridge.features.settings.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource

class SetNotificationsUseCase(private val source: SettingsDataSource) {
    suspend operator fun invoke(enabled: Boolean) = source.notifications(enabled)
}
