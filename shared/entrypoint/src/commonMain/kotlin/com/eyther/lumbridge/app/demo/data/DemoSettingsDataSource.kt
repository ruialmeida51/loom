package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.safeCall
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import kotlinx.coroutines.flow.*

class DemoSettingsDataSource : SettingsDataSource {
    private val mutable = MutableStateFlow(SettingsDomain(ThemePreferences.mode.value, false))
    override val preferences = mutable.asStateFlow()

    override suspend fun theme(value: String) = safeCall {
        ThemePreferences.set(value)
        mutable.update { it.copy(theme = value) }
    }

    override suspend fun notifications(enabled: Boolean) = safeCall {
        mutable.update { it.copy(notifications = enabled) }
    }
}
