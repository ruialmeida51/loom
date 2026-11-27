package com.eyther.lumbridge.di.account

import com.eyther.lumbridge.features.account.domain.datasource.PaymentHandlesDataSource
import com.eyther.lumbridge.features.account.domain.datasource.ProfileDataSource
import com.eyther.lumbridge.features.account.domain.usecase.GetPaymentHandlesUseCase
import com.eyther.lumbridge.features.account.domain.usecase.GetProfileStreamUseCase
import com.eyther.lumbridge.features.account.domain.usecase.SavePaymentHandlesUseCase
import com.eyther.lumbridge.features.account.domain.usecase.SetProfilePictureUseCase
import com.eyther.lumbridge.features.account.domain.usecase.UpdateNicknameUseCase
import com.eyther.lumbridge.features.settings.domain.datasource.SettingsDataSource
import com.eyther.lumbridge.features.settings.domain.usecase.GetAppLanguageStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetLanguageUseCase
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
    fun provideSetProfilePictureUseCase(
        profileDataSource: ProfileDataSource,
    ): SetProfilePictureUseCase = SetProfilePictureUseCase(profileDataSource = profileDataSource)

    @Single
    fun provideGetPaymentHandlesUseCase(
        paymentHandlesDataSource: PaymentHandlesDataSource,
    ): GetPaymentHandlesUseCase =
        GetPaymentHandlesUseCase(paymentHandlesDataSource = paymentHandlesDataSource)

    @Single
    fun provideSavePaymentHandlesUseCase(
        paymentHandlesDataSource: PaymentHandlesDataSource,
    ): SavePaymentHandlesUseCase =
        SavePaymentHandlesUseCase(paymentHandlesDataSource = paymentHandlesDataSource)

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
    fun provideGetAppLanguageStreamUseCase(
        settingsDataSource: SettingsDataSource,
    ): GetAppLanguageStreamUseCase =
        GetAppLanguageStreamUseCase(settingsDataSource = settingsDataSource)

    @Single
    fun provideSetLanguageUseCase(settingsDataSource: SettingsDataSource): SetLanguageUseCase =
        SetLanguageUseCase(settingsDataSource = settingsDataSource)

    @Single
    fun provideSetNotificationsUseCase(
        settingsDataSource: SettingsDataSource,
    ): SetNotificationsUseCase = SetNotificationsUseCase(settingsDataSource = settingsDataSource)
}
