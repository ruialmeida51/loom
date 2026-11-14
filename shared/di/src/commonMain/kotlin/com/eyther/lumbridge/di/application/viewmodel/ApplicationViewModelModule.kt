package com.eyther.lumbridge.di.application.viewmodel

import com.eyther.lumbridge.features.authentication.domain.usecase.GetGoogleSignInResultsStreamUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RegisterUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.RequestEmailCodeUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.SignInWithGoogleUseCase
import com.eyther.lumbridge.features.authentication.domain.usecase.VerifyEmailUseCase
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.EmailVerificationScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.LoginScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.RegisterScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.EmailVerificationScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.LoginScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.RegisterScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.usecase.CreateHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsUseCase
import com.eyther.lumbridge.features.household.domain.usecase.JoinHouseholdUseCase
import com.eyther.lumbridge.features.household.presentation.viewmodel.CreateHouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.HouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.JoinHouseholdScreenViewModel
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.CreateHouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.HouseholdScreenViewModelContract
import com.eyther.lumbridge.features.household.presentation.viewmodel.contracts.JoinHouseholdScreenViewModelContract
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Module

@Module
class ApplicationViewModelModule {

    @KoinViewModel(binds = [LoginScreenViewModelContract::class])
    fun provideLoginScreenViewModel(
        signInUseCase: SignInUseCase,
        requestEmailCodeUseCase: RequestEmailCodeUseCase,
        signInWithGoogleUseCase: SignInWithGoogleUseCase,
        getGoogleSignInResultsStreamUseCase: GetGoogleSignInResultsStreamUseCase,
    ): LoginScreenViewModelContract = LoginScreenViewModel(
        signInUseCase = signInUseCase,
        requestEmailCodeUseCase = requestEmailCodeUseCase,
        signInWithGoogleUseCase = signInWithGoogleUseCase,
        getGoogleSignInResultsStreamUseCase = getGoogleSignInResultsStreamUseCase,
    )

    @KoinViewModel(binds = [RegisterScreenViewModelContract::class])
    fun provideRegisterScreenViewModel(
        registerUseCase: RegisterUseCase,
    ): RegisterScreenViewModelContract = RegisterScreenViewModel(
        registerUseCase = registerUseCase,
    )

    @KoinViewModel(binds = [EmailVerificationScreenViewModelContract::class])
    fun provideEmailVerificationScreenViewModel(
        @InjectedParam params: EmailVerificationScreenViewModelContract.Params,
        verifyEmailUseCase: VerifyEmailUseCase,
        requestEmailCodeUseCase: RequestEmailCodeUseCase,
    ): EmailVerificationScreenViewModelContract = EmailVerificationScreenViewModel(
        params = params,
        verifyEmailUseCase = verifyEmailUseCase,
        requestEmailCodeUseCase = requestEmailCodeUseCase,
    )

    @KoinViewModel(binds = [HouseholdScreenViewModelContract::class])
    fun provideHouseholdScreenViewModel(
        getHouseholdsUseCase: GetHouseholdsUseCase,
    ): HouseholdScreenViewModelContract = HouseholdScreenViewModel(
        getHouseholdsUseCase = getHouseholdsUseCase,
    )

    @KoinViewModel(binds = [CreateHouseholdScreenViewModelContract::class])
    fun provideCreateHouseholdScreenViewModel(
        createHouseholdUseCase: CreateHouseholdUseCase,
    ): CreateHouseholdScreenViewModelContract = CreateHouseholdScreenViewModel(
        createHouseholdUseCase = createHouseholdUseCase,
    )

    @KoinViewModel(binds = [JoinHouseholdScreenViewModelContract::class])
    fun provideJoinHouseholdScreenViewModel(
        joinHouseholdUseCase: JoinHouseholdUseCase,
    ): JoinHouseholdScreenViewModelContract = JoinHouseholdScreenViewModel(
        joinHouseholdUseCase = joinHouseholdUseCase,
    )
}
