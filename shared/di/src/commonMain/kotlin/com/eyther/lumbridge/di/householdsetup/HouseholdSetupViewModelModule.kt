package com.eyther.lumbridge.di.householdsetup

import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.CreateHouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.JoinHouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.CreateHouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.JoinHouseholdScreenViewModelContract
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class HouseholdSetupViewModelModule {
    @KoinViewModel(binds = [HouseholdScreenViewModelContract::class])
    fun provideHouseholdScreenViewModel(
        getHouseholdsUseCase: GetHouseholdsUseCase,
        getHouseholdsStreamUseCase: GetHouseholdsStreamUseCase,
    ): HouseholdScreenViewModelContract =
        HouseholdScreenViewModel(
            getHouseholdsUseCase = getHouseholdsUseCase,
            getHouseholdsStreamUseCase = getHouseholdsStreamUseCase,
        )

    @KoinViewModel(binds = [CreateHouseholdScreenViewModelContract::class])
    fun provideCreateHouseholdScreenViewModel(
        createHouseholdUseCase: CreateHouseholdUseCase,
    ): CreateHouseholdScreenViewModelContract =
        CreateHouseholdScreenViewModel(
            createHouseholdUseCase = createHouseholdUseCase,
        )

    @KoinViewModel(binds = [JoinHouseholdScreenViewModelContract::class])
    fun provideJoinHouseholdScreenViewModel(
        joinHouseholdUseCase: JoinHouseholdUseCase,
    ): JoinHouseholdScreenViewModelContract =
        JoinHouseholdScreenViewModel(
            joinHouseholdUseCase = joinHouseholdUseCase,
        )
}
