package com.eyther.lumbridge.features.authentication.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeAuthenticationDataSource : AuthenticationDataSource {

    override val configured: Boolean = true

    private val session = MutableStateFlow<AuthSessionDomain?>(null)

    var signInResult: AppResult<Unit> = AppResult.Success(Unit)
    var resendResult: AppResult<Unit> = AppResult.Success(Unit)
    var verifiedEmail: String? = null
    var verifiedCode: String? = null
    var signInCodeCount: Int = 0

    var resendCount: Int = 0
    var googleSignInCount: Int = 0
    val googleResults = MutableSharedFlow<AppResult<Unit>>(replay = 1)

    override fun stream() = session

    override suspend fun signInWithGoogle(): AppResult<Unit> {
        googleSignInCount += 1
        return AppResult.Success(Unit)
    }

    override fun streamGoogleSignInResults(): Flow<AppResult<Unit>> = googleResults

    override suspend fun signIn(email: String, password: String): AppResult<Unit> = signInResult

    override suspend fun register(email: String, password: String): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun verifyEmail(email: String, code: String): AppResult<Unit> {
        verifiedEmail = email
        verifiedCode = code
        session.value = AuthSessionDomain(userId = "member", email = email)
        return AppResult.Success(Unit)
    }

    override suspend fun resendVerificationEmail(email: String): AppResult<Unit> {
        resendCount += 1
        return resendResult
    }

    override suspend fun sendSignInCode(email: String): AppResult<Unit> {
        signInCodeCount += 1
        return resendResult
    }

    override suspend fun signOut(): AppResult<Unit> {
        session.value = null
        return AppResult.Success(Unit)
    }
}
