package com.eyther.lumbridge.features.authentication.data.network.datasource

import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.data.safeApiCall
import com.eyther.lumbridge.features.authentication.domain.datasource.AuthenticationDataSource
import com.eyther.lumbridge.features.authentication.domain.model.AuthSessionDomain
import com.eyther.lumbridge.features.settings.domain.datasource.PushDeviceDataSource
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class CommonAuthenticationDataSource(
    private val access: SupabaseAccess,
    private val pushDevices: PushDeviceDataSource,
) : AuthenticationDataSource {
    private val mutableSession = MutableStateFlow<AuthSessionDomain?>(null)
    override val session = mutableSession.asStateFlow()
    override val configured
        get() = access.configured

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        access.client?.let { client ->
            scope.launch {
                client.auth.sessionStatus.collect { status ->
                    mutableSession.value =
                        when (status) {
                            is SessionStatus.Authenticated ->
                                status.session.user?.let {
                                    AuthSessionDomain(it.id, it.email.orEmpty())
                                }
                            else -> null
                        }
                    if (status is SessionStatus.Authenticated) pushDevices.syncRegistration()
                }
            }
        }
    }

    override suspend fun signIn(email: String, password: String) = safeApiCall {
        access.requireClient().auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun register(email: String, password: String) = safeApiCall {
        access.requireClient().auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
        Unit
    }

    override suspend fun signOut() = safeApiCall {
        try {
            pushDevices.unregisterCurrentDevice()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {}
        access.requireClient().auth.signOut()
        mutableSession.value = null
    }
}
