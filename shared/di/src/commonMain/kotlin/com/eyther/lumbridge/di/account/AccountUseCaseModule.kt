package com.eyther.lumbridge.di.account

import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.domain.usecase.GetProfileStreamUseCase
import com.eyther.lumbridge.features.account.domain.usecase.UpdateNicknameUseCase
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class AccountUseCaseModule {
    @Single
    fun provideGetProfileStreamUseCase(
        profileDataSource: ProfileDataSource,
    ): GetProfileStreamUseCase = GetProfileStreamUseCase(profileDataSource = profileDataSource)

    @Single
    fun provideUpdateNicknameUseCase(profileDataSource: ProfileDataSource): UpdateNicknameUseCase =
        UpdateNicknameUseCase(profileDataSource = profileDataSource)

    @Single
    fun provideGetSettingsStreamUseCase(
        settingsDataSource: SettingsDataSource,
    ): GetSettingsStreamUseCase = GetSettingsStreamUseCase(settingsDataSource = settingsDataSource)

    @Single
    fun provideSetThemeUseCase(settingsDataSource: SettingsDataSource): SetThemeUseCase =
        SetThemeUseCase(settingsDataSource = settingsDataSource)

    @Single
    fun provideSetNotificationsUseCase(
        settingsDataSource: SettingsDataSource,
    ): SetNotificationsUseCase = SetNotificationsUseCase(settingsDataSource = settingsDataSource)
}
