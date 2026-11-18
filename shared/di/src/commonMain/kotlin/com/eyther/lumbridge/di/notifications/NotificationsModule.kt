package com.eyther.lumbridge.di.notifications

import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.usecase.GetNotificationPermissionUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.MarkNotificationPrimeAnsweredUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.OpenNotificationSettingsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.presentation.viewmodel.NotificationsPrimeScreenViewModel
import com.eyther.lumbridge.features.settings.presentation.viewmodel.contracts.NotificationsPrimeScreenViewModelContract
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class NotificationsModule {
    @Single
    fun provideGetNotificationPermissionUseCase(
        settingsDataSource: SettingsDataSource,
    ): GetNotificationPermissionUseCase =
        GetNotificationPermissionUseCase(settingsDataSource = settingsDataSource)

    @Single
    fun provideMarkNotificationPrimeAnsweredUseCase(
        settingsDataSource: SettingsDataSource,
    ): MarkNotificationPrimeAnsweredUseCase =
        MarkNotificationPrimeAnsweredUseCase(settingsDataSource = settingsDataSource)

    @Single
    fun provideOpenNotificationSettingsUseCase(
        settingsDataSource: SettingsDataSource,
    ): OpenNotificationSettingsUseCase =
        OpenNotificationSettingsUseCase(settingsDataSource = settingsDataSource)

    @KoinViewModel(binds = [NotificationsPrimeScreenViewModelContract::class])
    fun provideNotificationsPrimeScreenViewModel(
        getSettings: GetSettingsStreamUseCase,
        getPermission: GetNotificationPermissionUseCase,
        markAnswered: MarkNotificationPrimeAnsweredUseCase,
        openSettings: OpenNotificationSettingsUseCase,
        setNotifications: SetNotificationsUseCase,
    ): NotificationsPrimeScreenViewModelContract =
        NotificationsPrimeScreenViewModel(
            getSettings = getSettings,
            getPermission = getPermission,
            markAnswered = markAnswered,
            openSettings = openSettings,
            setNotifications = setNotifications,
        )
}
