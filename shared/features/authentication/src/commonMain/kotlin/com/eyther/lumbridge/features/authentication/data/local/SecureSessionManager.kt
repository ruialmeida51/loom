package com.eyther.lumbridge.features.authentication.data.local

import com.eyther.lumbridge.core.data.PlatformStorage
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.exception.NoSessionFoundException
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.serialization.json.Json

class SecureSessionManager : SessionManager {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveSession(session: UserSession) =
        PlatformStorage.put("session", json.encodeToString(session))

    override suspend fun loadSession(): UserSession =
        loadSessionOrNull() ?: throw NoSessionFoundException()

    override suspend fun loadSessionOrNull(): UserSession? =
        PlatformStorage.get("session")?.let { value ->
            runCatching { json.decodeFromString<UserSession>(value) }.getOrNull()
        }

    override suspend fun deleteSession() = PlatformStorage.put("session", null)
}
