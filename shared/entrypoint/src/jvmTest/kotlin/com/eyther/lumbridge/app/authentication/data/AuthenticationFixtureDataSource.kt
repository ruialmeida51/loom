package com.eyther.lumbridge.app.authentication.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import kotlinx.coroutines.flow.MutableStateFlow

internal class AuthenticationFixtureDataSource : AuthenticationDataSource {

    override val configured: Boolean = true

    private val session = MutableStateFlow<AuthSessionDomain?>(null)

    var signInCodeCount: Int = 0

    var resendCount: Int = 0
        private set

    override fun stream() = session

    override suspend fun signIn(email: String, password: String): AppResult<Unit> =
        AppResult.Failure(AuthenticationError.EmailNotConfirmed())

    override suspend fun register(email: String, password: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun verifyEmail(email: String, code: String): AppResult<Unit> {
        if (code != "123456") {
            return AppResult.Failure(AuthenticationError.VerificationCodeExpired())
        }

        session.value = AuthSessionDomain(userId = "test-member", email = email)
        return AppResult.Success(Unit)
    }

    override suspend fun resendVerificationEmail(email: String): AppResult<Unit> {
        resendCount += 1
        return AppResult.Success(Unit)
    }

    override suspend fun sendSignInCode(email: String): AppResult<Unit> {
        signInCodeCount += 1
        return AppResult.Success(Unit)
    }

    override suspend fun signOut(): AppResult<Unit> {
        session.value = null
        return AppResult.Success(Unit)
    }
}
