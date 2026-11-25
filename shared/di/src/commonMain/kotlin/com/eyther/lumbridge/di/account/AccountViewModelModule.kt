package com.eyther.lumbridge.di.account

import com.eyther.lumbridge.features.account.domain.usecase.SetProfilePictureUseCase
import com.eyther.lumbridge.features.account.domain.usecase.GetProfileStreamUseCase
import com.eyther.lumbridge.features.account.domain.usecase.UpdateNicknameUseCase
import com.eyther.lumbridge.features.account.presentation.viewmodel.AccountScreenViewModel
import com.eyther.lumbridge.features.account.presentation.viewmodel.contracts.AccountScreenViewModelContract
import com.eyther.lumbridge.features.settings.domain.usecase.GetSettingsStreamUseCase
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
        setNotificationsUseCase: SetNotificationsUseCase,
        setProfilePictureUseCase: SetProfilePictureUseCase,
    ): AccountScreenViewModelContract =
        AccountScreenViewModel(
            params = params,
            getProfileStreamUseCase = getProfileStreamUseCase,
            updateNicknameUseCase = updateNicknameUseCase,
            getSettingsStreamUseCase = getSettingsStreamUseCase,
            setThemeUseCase = setThemeUseCase,
            setNotificationsUseCase = setNotificationsUseCase,
            setProfilePictureUseCase = setProfilePictureUseCase,
        )
}
