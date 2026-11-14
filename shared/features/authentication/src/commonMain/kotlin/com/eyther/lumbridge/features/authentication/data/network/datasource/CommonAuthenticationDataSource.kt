package com.eyther.lumbridge.features.authentication.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.network.config.AuthenticationRedirect
import com.eyther.lumbridge.features.authentication.data.network.mapper.toAuthenticationError
import com.eyther.lumbridge.features.authentication.data.network.mapper.toGoogleSignInError
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.errors.AuthenticationError
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.OtpVerifyResult
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CommonAuthenticationDataSource(
    private val access: SupabaseAccess,
    private val pushDevices: PushDeviceDataSource,
    callbackEvents: Flow<AppResult<String>> = emptyFlow(),
) : AuthenticationDataSource {

    private val mutableSession = MutableStateFlow<AuthSessionDomain?>(null)
    private val googleSignInResult = MutableStateFlow<AppResult<Unit>?>(null)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val configured: Boolean = access.configured

    init {
        access.client?.let { client ->
            scope.launch {
                client.auth.sessionStatus.collect { status ->
                    mutableSession.value = when (status) {
                        is SessionStatus.Authenticated -> status.session.user?.let { user ->
                            AuthSessionDomain(userId = user.id, email = user.email.orEmpty())
                        }
                        else -> null
                    }

                    if (status is SessionStatus.Authenticated) {
                        pushDevices.syncRegistration()
                    }
                }
            }
            scope.launch {
                callbackEvents.collect { callback ->
                    googleSignInResult.value = when (callback) {
                        is AppResult.Failure -> callback
                        is AppResult.Success -> safeApiCall(mapError = Exception::toGoogleSignInError) {
                            client.auth.awaitInitialization()
                            client.auth.exchangeCodeForSession(code = callback.value)
                            Unit
                        }
                    }
                }
            }
        }
    }

    override fun stream(): StateFlow<AuthSessionDomain?> = mutableSession.asStateFlow()

    override fun streamGoogleSignInResults(): Flow<AppResult<Unit>> = googleSignInResult.filterNotNull()

    override suspend fun signInWithGoogle(): AppResult<Unit> {
        googleSignInResult.value = null
        return safeApiCall(mapError = Exception::toGoogleSignInError) {
            val client = access.requireClient()
            client.auth.awaitInitialization()
            withContext(Dispatchers.Main) {
                client.auth.signInWith(Google, redirectUrl = AuthenticationRedirect.URL)
            }
        }
    }

    override suspend fun signIn(email: String, password: String): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toAuthenticationError,
        ) {
            access.requireClient().auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }
    }

    override suspend fun register(email: String, password: String): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toAuthenticationError,
        ) {
            access.requireClient().auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            Unit
        }
    }

    override suspend fun verifyEmail(email: String, code: String): AppResult<Unit> {
        return safeApiCall(mapError = Exception::toAuthenticationError) {
            val result = access.requireClient().auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = email,
                token = code,
            )
            if (result !is OtpVerifyResult.Authenticated) {
                throw AuthenticationError.VerificationFailed()
            }
        }
    }

    override suspend fun resendVerificationEmail(email: String): AppResult<Unit> {
        return safeApiCall(mapError = Exception::toAuthenticationError) {
            access.requireClient().auth.resendEmail(type = OtpType.Email.SIGNUP, email = email)
        }
    }

    override suspend fun sendSignInCode(email: String): AppResult<Unit> {
        return safeApiCall(mapError = Exception::toAuthenticationError) {
            access.requireClient().auth.signInWith(OTP) {
                this.email = email
                createUser = false
            }
        }
    }

    override suspend fun signOut(): AppResult<Unit> {
        googleSignInResult.value = null
        return safeApiCall(
            mapError = Exception::toAuthenticationError,
        ) {
            pushDevices.unregisterCurrentDevice()
            access.requireClient().auth.signOut()
            mutableSession.value = null
        }
    }
}
