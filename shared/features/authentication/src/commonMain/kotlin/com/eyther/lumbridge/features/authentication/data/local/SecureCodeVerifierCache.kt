package com.eyther.lumbridge.features.authentication.data.local

import com.eyther.lumbridge.core.data.local.PlatformStorage
import io.github.jan.supabase.auth.CodeVerifierCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

/** Keeps the PKCE verifier across process death using the same encrypted native store as sessions. */
class SecureCodeVerifierCache : CodeVerifierCache {

    override suspend fun saveCodeVerifier(codeVerifier: String): Unit = withContext(Dispatchers.IO) {
        PlatformStorage.put("auth_pkce_verifier", codeVerifier)
    }

    override suspend fun loadCodeVerifier(): String? = withContext(Dispatchers.IO) {
        PlatformStorage.get("auth_pkce_verifier")
    }

    override suspend fun deleteCodeVerifier(): Unit = withContext(Dispatchers.IO) {
        PlatformStorage.put("auth_pkce_verifier", null)
    }
}
