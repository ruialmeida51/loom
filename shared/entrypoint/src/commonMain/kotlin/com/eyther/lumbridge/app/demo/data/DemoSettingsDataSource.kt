package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DemoSettingsDataSource : SettingsDataSource {

    private val mutable = MutableStateFlow(SettingsDomain(ThemePreferences.mode.value, false))
    override fun stream() = mutable.asStateFlow()

    override suspend fun setTheme(value: String) = safeCall {
        ThemePreferences.set(value)
        mutable.update { it.copy(theme = value) }
    }

    override suspend fun setNotifications(enabled: Boolean) = safeCall {
        mutable.update { it.copy(notifications = enabled) }
    }
}
