package com.eyther.lumbridge.di.application.viewmodel

import com.eyther.lumbridge.features.authentication.domain.usecase.RegisterUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInUseCase
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.AuthenticationScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.AuthenticationScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class ApplicationViewModelModule {

    @KoinViewModel(binds = [AuthenticationScreenViewModelContract::class])
    fun provideAuthenticationScreenViewModel(
        signInUseCase: SignInUseCase,
        registerUseCase: RegisterUseCase,
    ): AuthenticationScreenViewModelContract = AuthenticationScreenViewModel(
        signInUseCase = signInUseCase,
        registerUseCase = registerUseCase,
    )

    @KoinViewModel(binds = [HouseholdScreenViewModelContract::class])
    fun provideHouseholdScreenViewModel(
        getHouseholdsUseCase: GetHouseholdsUseCase,
        createHouseholdUseCase: CreateHouseholdUseCase,
        joinHouseholdUseCase: JoinHouseholdUseCase,
    ): HouseholdScreenViewModelContract = HouseholdScreenViewModel(
        getHouseholdsUseCase = getHouseholdsUseCase,
        createHouseholdUseCase = createHouseholdUseCase,
        joinHouseholdUseCase = joinHouseholdUseCase,
    )
}
