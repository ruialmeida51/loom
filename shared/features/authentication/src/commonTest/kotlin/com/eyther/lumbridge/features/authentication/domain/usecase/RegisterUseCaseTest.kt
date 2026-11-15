package com.eyther.lumbridge.features.authentication.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import com.eyther.lumbridge.features.authentication.domain.model.AuthenticationStateDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest

class RegisterUseCaseTest {

    private class RegistrationDataSource : AuthenticationDataSource {

        override val configured: Boolean = true

        var registeredEmail: String? = null

        override fun stream(): StateFlow<AuthenticationStateDomain> =
            MutableStateFlow(AuthenticationStateDomain.SignedOut)

        override suspend fun signInWithGoogle(): AppResult<Unit> = AppResult.Success(Unit)

        override fun streamGoogleSignInResults(): Flow<AppResult<Unit>> = emptyFlow()

        override suspend fun signIn(email: String, password: String): AppResult<Unit> =
            error("Sign-in is outside this test")

        override suspend fun register(email: String, password: String): AppResult<Unit> {
            registeredEmail = email
            return AppResult.Success(Unit)
        }

        override suspend fun verifyEmail(email: String, code: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun resendVerificationEmail(email: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun sendSignInCode(email: String): AppResult<Unit> =
            AppResult.Success(Unit)

        override suspend fun signOut(): AppResult<Unit> = error("Sign-out is outside this test")
    }

    @Test
    fun registrationRequiresExplicitTermsAcceptanceBeforeContactingTheServer() = runTest {
        val dataSource = RegistrationDataSource()
        val register = RegisterUseCase(authenticationDataSource = dataSource)

        val result =
            register(
                email = "roommate@example.test",
                password = "test-password-long-enough",
                acceptedTerms = false,
            )

        assertIs<AuthenticationError.TermsNotAccepted>(assertIs<AppResult.Failure>(result).error)
        assertNull(dataSource.registeredEmail)
    }

    @Test
    fun acceptedTermsDoNotBypassCredentialValidation() = runTest {
        val dataSource = RegistrationDataSource()
        val register = RegisterUseCase(authenticationDataSource = dataSource)

        val result = register(email = "invalid", password = "short", acceptedTerms = true)

        assertIs<AuthenticationError.InvalidRegistration>(assertIs<AppResult.Failure>(result).error)
        assertNull(dataSource.registeredEmail)
    }

    @Test
    fun validRegistrationForwardsOnlyValidatedCredentials() = runTest {
        val dataSource = RegistrationDataSource()
        val register = RegisterUseCase(authenticationDataSource = dataSource)

        val result =
            register(
                email = " roommate@example.test ",
                password = "test-password-long-enough",
                acceptedTerms = true,
            )

        assertIs<AppResult.Success<Unit>>(result)
        assertEquals("roommate@example.test", dataSource.registeredEmail)
    }
}
