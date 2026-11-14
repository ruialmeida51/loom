package com.eyther.lumbridge.di.application.usecase

import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.usecase.GetSessionStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RegisterUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdDataSource
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource
import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ApplicationUseCaseModule {

    @Single
    fun provideGetSessionStreamUseCase(
        dataSource: AuthenticationDataSource,
    ): GetSessionStreamUseCase = GetSessionStreamUseCase(
        dataSource = dataSource,
    )

    @Single
    fun provideRegisterUseCase(
        source: AuthenticationDataSource,
    ): RegisterUseCase = RegisterUseCase(
        source = source,
    )

    @Single
    fun provideSignInUseCase(
        source: AuthenticationDataSource,
    ): SignInUseCase = SignInUseCase(
        source = source,
    )

    @Single
    fun provideSignOutUseCase(
        source: AuthenticationDataSource,
    ): SignOutUseCase = SignOutUseCase(
        source = source,
    )

    @Single
    fun provideCreateHouseholdUseCase(
        householdDataSource: HouseholdDataSource,
    ): CreateHouseholdUseCase = CreateHouseholdUseCase(
        householdDataSource = householdDataSource,
    )

    @Single
    fun provideGetHouseholdsUseCase(
        householdDataSource: HouseholdDataSource,
    ): GetHouseholdsUseCase = GetHouseholdsUseCase(
        householdDataSource = householdDataSource,
    )

    @Single
    fun provideJoinHouseholdUseCase(
        householdDataSource: HouseholdDataSource,
    ): JoinHouseholdUseCase = JoinHouseholdUseCase(
        householdDataSource = householdDataSource,
    )

    @Single
    fun provideRememberHouseholdUseCase(
        selectionDataSource: HouseholdSelectionDataSource,
    ): RememberHouseholdUseCase = RememberHouseholdUseCase(
        selectionDataSource = selectionDataSource,
    )

    @Single
    fun provideRestoreHouseholdUseCase(
        selectionDataSource: HouseholdSelectionDataSource,
        getHouseholdsUseCase: GetHouseholdsUseCase,
    ): RestoreHouseholdUseCase = RestoreHouseholdUseCase(
        selectionDataSource = selectionDataSource,
        getHouseholdsUseCase = getHouseholdsUseCase,
    )
}
