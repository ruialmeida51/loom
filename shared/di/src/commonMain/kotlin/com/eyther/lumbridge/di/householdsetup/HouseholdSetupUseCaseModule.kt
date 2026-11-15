package com.eyther.lumbridge.di.householdsetup

import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class HouseholdSetupUseCaseModule {
    @Single
    fun provideCreateHouseholdUseCase(
        householdDataSource: HouseholdDataSource,
    ): CreateHouseholdUseCase =
        CreateHouseholdUseCase(
            householdDataSource = householdDataSource,
        )

    @Single
    fun provideGetHouseholdsUseCase(
        householdDataSource: HouseholdDataSource,
    ): GetHouseholdsUseCase =
        GetHouseholdsUseCase(
            householdDataSource = householdDataSource,
        )

    @Single
    fun provideJoinHouseholdUseCase(
        householdDataSource: HouseholdDataSource,
    ): JoinHouseholdUseCase =
        JoinHouseholdUseCase(
            householdDataSource = householdDataSource,
        )
}
