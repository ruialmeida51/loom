package com.eyther.lumbridge.features.authentication.data.local

import com.eyther.lumbridge.core.data.local.PlatformStorage
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.exception.NoSessionFoundException
import io.github.jan.supabase.auth.user.UserSession
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

private const val SESSION_STORAGE_KEY = "session"

class SecureSessionManager : SessionManager {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun saveSession(session: UserSession) =
        PlatformStorage.put(SESSION_STORAGE_KEY, json.encodeToString(session))

    override suspend fun loadSession(): UserSession =
        loadSessionOrNull() ?: throw NoSessionFoundException()

    override suspend fun loadSessionOrNull(): UserSession? =
        PlatformStorage.get(SESSION_STORAGE_KEY)?.let(::restoreSession)

    override suspend fun deleteSession() = PlatformStorage.put(SESSION_STORAGE_KEY, null)

    /**
     * The SDK's default expiry is relative to now; legacy payloads without it must refresh first.
     */
    private fun restoreSession(value: String): UserSession? =
        try {
            val document = json.parseToJsonElement(value).jsonObject
            val session = json.decodeFromJsonElement<UserSession>(document)
            if (document.containsKey("expiresAt")) session
            else session.copy(expiresAt = Instant.fromEpochSeconds(0))
        } catch (_: IllegalArgumentException) {
            null
        }
}
