package com.eyther.lumbridge.di.application.viewmodel

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
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.EmailVerificationScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.LoginScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.RegisterScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.SessionScreenViewModel
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.EmailVerificationScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.LoginScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.RegisterScreenViewModelContract
import com.eyther.lumbridge.features.authentication.presentation.viewmodel.contracts.SessionScreenViewModelContract
import com.eyther.lumbridge.features.household.domain.usecase.GetHouseholdsStreamUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RememberHouseholdUseCase
import com.eyther.lumbridge.features.household.domain.usecase.RestoreHouseholdUseCase
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
    ): LoginScreenViewModelContract =
        LoginScreenViewModel(
            signInUseCase = signInUseCase,
            requestEmailCodeUseCase = requestEmailCodeUseCase,
            signInWithGoogleUseCase = signInWithGoogleUseCase,
            getGoogleSignInResultsStreamUseCase = getGoogleSignInResultsStreamUseCase,
        )

    @KoinViewModel(binds = [RegisterScreenViewModelContract::class])
    fun provideRegisterScreenViewModel(
        registerUseCase: RegisterUseCase,
    ): RegisterScreenViewModelContract =
        RegisterScreenViewModel(
            registerUseCase = registerUseCase,
        )

    @KoinViewModel(binds = [EmailVerificationScreenViewModelContract::class])
    fun provideEmailVerificationScreenViewModel(
        @InjectedParam params: EmailVerificationScreenViewModelContract.Params,
        verifyEmailUseCase: VerifyEmailUseCase,
        requestEmailCodeUseCase: RequestEmailCodeUseCase,
    ): EmailVerificationScreenViewModelContract =
        EmailVerificationScreenViewModel(
            params = params,
            verifyEmailUseCase = verifyEmailUseCase,
            requestEmailCodeUseCase = requestEmailCodeUseCase,
        )

    @KoinViewModel(binds = [SessionScreenViewModelContract::class])
    fun provideSessionScreenViewModel(
        getSessionStreamUseCase: GetSessionStreamUseCase,
        recoverSessionUseCase: RecoverSessionUseCase,
        resumeSessionUseCase: ResumeSessionUseCase,
        restoreHouseholdUseCase: RestoreHouseholdUseCase,
        getHouseholdsStreamUseCase: GetHouseholdsStreamUseCase,
        rememberHouseholdUseCase: RememberHouseholdUseCase,
        signOutUseCase: SignOutUseCase,
    ): SessionScreenViewModelContract =
        SessionScreenViewModel(
            recoverSessionUseCase = recoverSessionUseCase,
            resumeSessionUseCase = resumeSessionUseCase,
            getSessionStreamUseCase = getSessionStreamUseCase,
            restoreHouseholdUseCase = restoreHouseholdUseCase,
            getHouseholdsStreamUseCase = getHouseholdsStreamUseCase,
            rememberHouseholdUseCase = rememberHouseholdUseCase,
            signOutUseCase = signOutUseCase,
        )
}
