package com.eyther.lumbridge.di.application.usecase

import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.usecase.GetGoogleSignInResultsStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.GetSessionStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RecoverSessionUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RegisterUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RequestEmailCodeUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.ResumeSessionUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInWithGoogleUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignOutUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.VerifyEmailUseCase
import com.eyther.lumbridge.features.household.domain.datasource.HouseholdSelectionDataSource
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
class ApplicationUseCaseModule {

    @Single
    fun provideResumeSessionUseCase(
        authenticationDataSource: AuthenticationDataSource,
    ): ResumeSessionUseCase =
        ResumeSessionUseCase(authenticationDataSource = authenticationDataSource)

    @Single
    fun provideRecoverSessionUseCase(
        authenticationDataSource: AuthenticationDataSource,
    ): RecoverSessionUseCase =
        RecoverSessionUseCase(authenticationDataSource = authenticationDataSource)

    @Single
    fun provideSignInWithGoogleUseCase(
        authenticationDataSource: AuthenticationDataSource,
    ): SignInWithGoogleUseCase =
        SignInWithGoogleUseCase(
            authenticationDataSource = authenticationDataSource,
        )

    @Single
    fun provideGetGoogleSignInResultsStreamUseCase(
        authenticationDataSource: AuthenticationDataSource,
    ): GetGoogleSignInResultsStreamUseCase =
        GetGoogleSignInResultsStreamUseCase(
            authenticationDataSource = authenticationDataSource,
        )

    @Single
    fun provideGetSessionStreamUseCase(
        dataSource: AuthenticationDataSource,
    ): GetSessionStreamUseCase =
        GetSessionStreamUseCase(
            dataSource = dataSource,
        )

    @Single
    fun provideRegisterUseCase(
        authenticationDataSource: AuthenticationDataSource,
    ): RegisterUseCase =
        RegisterUseCase(
            authenticationDataSource = authenticationDataSource,
        )

    @Single
    fun provideVerifyEmailUseCase(
        authenticationDataSource: AuthenticationDataSource,
    ): VerifyEmailUseCase =
        VerifyEmailUseCase(
            authenticationDataSource = authenticationDataSource,
        )

    @Single
    fun provideRequestEmailCodeUseCase(
        authenticationDataSource: AuthenticationDataSource,
    ): RequestEmailCodeUseCase =
        RequestEmailCodeUseCase(
            authenticationDataSource = authenticationDataSource,
        )

    @Single
    fun provideSignInUseCase(
        authenticationDataSource: AuthenticationDataSource,
    ): SignInUseCase =
        SignInUseCase(
            authenticationDataSource = authenticationDataSource,
        )

    @Single
    fun provideSignOutUseCase(
        source: AuthenticationDataSource,
    ): SignOutUseCase =
        SignOutUseCase(
            source = source,
        )

    @Single
    fun provideRememberHouseholdUseCase(
        selectionDataSource: HouseholdSelectionDataSource,
    ): RememberHouseholdUseCase =
        RememberHouseholdUseCase(
            selectionDataSource = selectionDataSource,
        )

    @Single
    fun provideRestoreHouseholdUseCase(
        selectionDataSource: HouseholdSelectionDataSource,
        getHouseholdsUseCase: GetHouseholdsUseCase,
    ): RestoreHouseholdUseCase =
        RestoreHouseholdUseCase(
            selectionDataSource = selectionDataSource,
            getHouseholdsUseCase = getHouseholdsUseCase,
        )
}
