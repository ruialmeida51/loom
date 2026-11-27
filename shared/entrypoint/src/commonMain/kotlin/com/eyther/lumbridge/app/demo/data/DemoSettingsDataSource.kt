package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.AppLanguage
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.settings.data.local.LanguagePreferences
import com.eyther.lumbridge.features.settings.data.local.ThemePreferences
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.model.NotificationPermissionDomain
import com.eyther.lumbridge.features.settings.domain.model.SettingsDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DemoSettingsDataSource : SettingsDataSource {

    private val mutable =
        MutableStateFlow(
            SettingsDomain(
                theme = ThemePreferences.mode.value,
                language = LanguagePreferences.language.value,
            ),
        )

    override fun stream() = mutable.asStateFlow()

    override suspend fun getNotificationPermission() = safeCall {
        NotificationPermissionDomain.UNAVAILABLE
    }

    override suspend fun markNotificationPrimeAnswered() = safeCall {
        mutable.update { oldState -> oldState.copy(notificationPrimeAnswered = true) }
    }

    override suspend fun openNotificationSettings() = safeCall { Unit }

    override suspend fun setTheme(value: String) = safeCall {
        ThemePreferences.set(value)
        mutable.update { it.copy(theme = value) }
    }

    override suspend fun setLanguage(language: AppLanguage) = safeCall {
        LanguagePreferences.set(language)
        mutable.update { it.copy(language = language) }
    }

    override suspend fun setNotifications(enabled: Boolean) = safeCall {
        mutable.update { it.copy(notifications = enabled) }
    }
}
