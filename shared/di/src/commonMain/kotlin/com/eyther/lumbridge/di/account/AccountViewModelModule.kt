package com.eyther.lumbridge.di.account

import com.eyther.lumbridge.features.account.domain.usecase.GetPaymentHandlesUseCase
import com.eyther.lumbridge.features.account.domain.usecase.GetProfileStreamUseCase
import com.eyther.lumbridge.features.account.domain.usecase.SavePaymentHandlesUseCase
import com.eyther.lumbridge.features.account.domain.usecase.SetProfilePictureUseCase
import com.eyther.lumbridge.features.account.domain.usecase.UpdateNicknameUseCase
import com.eyther.lumbridge.features.account.presentation.viewmodel.AccountScreenViewModel
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountScreenViewModelContract
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetLanguageUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetNotificationsUseCase
import com.eyther.lumbridge.features.settings.domain.usecase.SetThemeUseCase
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class AccountViewModelModule {
    @KoinViewModel(binds = [AccountScreenViewModelContract::class])
    fun provideAccountScreenViewModel(
        @InjectedParam params: AccountScreenViewModelContract.Params,
        getProfileStreamUseCase: GetProfileStreamUseCase,
        updateNicknameUseCase: UpdateNicknameUseCase,
        getSettingsStreamUseCase: GetSettingsStreamUseCase,
        setThemeUseCase: SetThemeUseCase,
        setLanguageUseCase: SetLanguageUseCase,
        setNotificationsUseCase: SetNotificationsUseCase,
        setProfilePictureUseCase: SetProfilePictureUseCase,
        getPaymentHandlesUseCase: GetPaymentHandlesUseCase,
        savePaymentHandlesUseCase: SavePaymentHandlesUseCase,
    ): AccountScreenViewModelContract =
        AccountScreenViewModel(
            params = params,
            getProfileStreamUseCase = getProfileStreamUseCase,
            updateNicknameUseCase = updateNicknameUseCase,
            getSettingsStreamUseCase = getSettingsStreamUseCase,
            setThemeUseCase = setThemeUseCase,
            setLanguageUseCase = setLanguageUseCase,
            setNotificationsUseCase = setNotificationsUseCase,
            setProfilePictureUseCase = setProfilePictureUseCase,
            getPaymentHandlesUseCase = getPaymentHandlesUseCase,
            savePaymentHandlesUseCase = savePaymentHandlesUseCase,
        )
}
