package com.eyther.lumbridge.features.authentication.presentation

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.FakeAuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import com.eyther.lumbridge.features.authentication.domain.model.EmailCodePurposeDomain
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class AuthenticationViewModelTest {

    @Test
    fun googleSignInNeedsNoEmailInputAndCallbackFailuresStayTypedInTheViewModel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FakeAuthenticationDataSource()
        val viewModel = LoginScreenViewModel(
            signInUseCase = SignInUseCase(authenticationDataSource = source),
            requestEmailCodeUseCase = RequestEmailCodeUseCase(authenticationDataSource = source),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(authenticationDataSource = source),
            getGoogleSignInResultsStreamUseCase = GetGoogleSignInResultsStreamUseCase(authenticationDataSource = source),
        )
        try {
            backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            viewModel.onGoogleSignIn()
            runCurrent()
            assertEquals(1, source.googleSignInCount)
            assertEquals(0, source.signInCodeCount)
            assertFalse(viewModel.state.value.busy)
            assertNull(source.stream().value)

            source.googleResults.emit(AppResult.Failure(AuthenticationError.GoogleSignInCancelled()))
            runCurrent()
            assertNotNull(viewModel.state.value.displayError)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun unconfirmedSignInOpensVerificationAndClearsThePassword() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FakeAuthenticationDataSource().apply {
            signInResult = AppResult.Failure(AuthenticationError.EmailNotConfirmed())
        }
        val viewModel = LoginScreenViewModel(
            signInUseCase = SignInUseCase(authenticationDataSource = source),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(authenticationDataSource = source),
            getGoogleSignInResultsStreamUseCase = GetGoogleSignInResultsStreamUseCase(authenticationDataSource = source),
            requestEmailCodeUseCase = RequestEmailCodeUseCase(authenticationDataSource = source),
        )
        try {
            backgroundScope.launch { viewModel.state.collect() }
            viewModel.onEmailChanged(value = " member@example.com ")
            viewModel.onPasswordChanged(value = "private password")
            runCurrent()
            viewModel.onSubmit()
            runCurrent()

            val redirect = assertIs<LoginScreenViewModelContract.RedirectTo.VerifyEmail>(viewModel.state.value.redirectTo)
            assertEquals("member@example.com", redirect.email)
            assertEquals("", viewModel.state.value.password)
            assertNull(viewModel.state.value.error)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun invalidCredentialsStayOnLoginWithoutClaimingAnEmailWasSent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FakeAuthenticationDataSource().apply {
            signInResult = AppResult.Failure(AuthenticationError.InvalidCredentials())
        }
        val viewModel = LoginScreenViewModel(
            signInUseCase = SignInUseCase(authenticationDataSource = source),
            signInWithGoogleUseCase = SignInWithGoogleUseCase(authenticationDataSource = source),
            getGoogleSignInResultsStreamUseCase = GetGoogleSignInResultsStreamUseCase(authenticationDataSource = source),
            requestEmailCodeUseCase = RequestEmailCodeUseCase(authenticationDataSource = source),
        )
        try {
            backgroundScope.launch { viewModel.state.collect() }
            viewModel.onEmailChanged(value = "member@example.com")
            viewModel.onPasswordChanged(value = "incorrect password")
            runCurrent()
            viewModel.onSubmit()
            runCurrent()

            assertNotNull(viewModel.state.value.error)
            assertNull(viewModel.state.value.redirectTo)
            assertFalse(viewModel.state.value.busy)
            assertEquals(0, source.signInCodeCount)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun registrationOpensVerificationWithOnlyTheEmail() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val viewModel = RegisterScreenViewModel(
            registerUseCase = RegisterUseCase(authenticationDataSource = FakeAuthenticationDataSource()),
        )
        try {
            backgroundScope.launch { viewModel.state.collect() }
            viewModel.onEmailChanged(value = " member@example.com ")
            viewModel.onPasswordChanged(value = "private password")
            viewModel.onTermsAcceptedChanged(accepted = true)
            runCurrent()
            viewModel.onSubmit()
            runCurrent()

            val redirect = assertIs<RegisterScreenViewModelContract.RedirectTo.VerifyEmail>(viewModel.state.value.redirectTo)
            assertEquals("member@example.com", redirect.email)
            assertEquals("", viewModel.state.value.password)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun resendIsRateLimitedInTheViewModelAndVerificationPublishesASession() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FakeAuthenticationDataSource()
        val viewModel = EmailVerificationScreenViewModel(
            params = EmailVerificationScreenViewModelContract.Params(email = "member@example.com", codeRequested = false),
            verifyEmailUseCase = VerifyEmailUseCase(authenticationDataSource = source),
            requestEmailCodeUseCase = RequestEmailCodeUseCase(authenticationDataSource = source),
        )
        try {
            backgroundScope.launch { viewModel.state.collect() }
            runCurrent()
            viewModel.onResendCode()
            viewModel.onResendCode()
            runCurrent()
            assertEquals(1, source.resendCount)
            assertFalse(viewModel.state.value.canResend)
            viewModel.onResendCode()
            runCurrent()
            assertEquals(1, source.resendCount)

            advanceTimeBy(60_000)
            runCurrent()
            assertTrue(viewModel.state.value.canResend)
            viewModel.onCodeChanged(value = "001234")
            runCurrent()
            viewModel.onVerify()
            runCurrent()
            assertEquals("001234", source.verifiedCode)
            assertEquals("member@example.com", source.verifiedEmail)
            assertNotNull(source.stream().value)
            assertEquals("", viewModel.state.value.code)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun signInVerificationResendsALoginCodeRatherThanASignupConfirmation() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FakeAuthenticationDataSource()
        val viewModel = EmailVerificationScreenViewModel(
            params = EmailVerificationScreenViewModelContract.Params(
                email = "member@example.com",
                codeRequested = false,
                purpose = EmailCodePurposeDomain.SIGN_IN,
            ),
            verifyEmailUseCase = VerifyEmailUseCase(authenticationDataSource = source),
            requestEmailCodeUseCase = RequestEmailCodeUseCase(authenticationDataSource = source),
        )
        try {
            viewModel.onResendCode()
            runCurrent()
            assertEquals(1, source.signInCodeCount)
            assertEquals(0, source.resendCount)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun resendFailureDoesNotShowASuccessMessage() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val source = FakeAuthenticationDataSource().apply {
            resendResult = AppResult.Failure(AuthenticationError.EmailDeliveryUnavailable())
        }
        val viewModel = EmailVerificationScreenViewModel(
            params = EmailVerificationScreenViewModelContract.Params(email = "member@example.com", codeRequested = false),
            verifyEmailUseCase = VerifyEmailUseCase(authenticationDataSource = source),
            requestEmailCodeUseCase = RequestEmailCodeUseCase(authenticationDataSource = source),
        )
        try {
            viewModel.onResendCode()
            runCurrent()
            assertNotNull(viewModel.state.value.error)
            assertNull(viewModel.state.value.message)
            assertTrue(viewModel.state.value.canResend)
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}
