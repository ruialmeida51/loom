package com.eyther.lumbridge.features.authentication.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.authentication.data.network.mapper.toAuthenticationError
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CommonAuthenticationDataSource(
    private val access: SupabaseAccess,
    private val pushDevices: PushDeviceDataSource,
) : AuthenticationDataSource {

    private val mutableSession = MutableStateFlow<AuthSessionDomain?>(null)
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
        }
    }

    override fun stream(): StateFlow<AuthSessionDomain?> = mutableSession.asStateFlow()

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

    override suspend fun signOut(): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toAuthenticationError,
        ) {
            pushDevices.unregisterCurrentDevice()
            access.requireClient().auth.signOut()
            mutableSession.value = null
        }
    }
}
