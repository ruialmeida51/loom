package com.eyther.lumbridge.features.authentication.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthenticationDataSource {
    fun stream(): StateFlow<AuthSessionDomain?>
    val configured: Boolean

    suspend fun signIn(email: String, password: String): AppResult<Unit>

    /** Starts the system-browser flow; authenticated entry still follows [stream]. */
    suspend fun signInWithGoogle(): AppResult<Unit>

    fun streamGoogleSignInResults(): Flow<AppResult<Unit>>

    suspend fun register(email: String, password: String): AppResult<Unit>

    /** Verifies the email code and publishes the resulting session through [stream]. */
    suspend fun verifyEmail(email: String, code: String): AppResult<Unit>

    suspend fun resendVerificationEmail(email: String): AppResult<Unit>

    /** Requests access for an existing account without silently registering a new one. */
    suspend fun sendSignInCode(email: String): AppResult<Unit>

    suspend fun signOut(): AppResult<Unit>
}
