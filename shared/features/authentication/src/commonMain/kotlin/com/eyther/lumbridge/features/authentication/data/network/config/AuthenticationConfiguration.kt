package com.eyther.lumbridge.features.authentication.data.network.config

import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.CodeVerifierCache
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.auth.SessionManager

fun AuthConfig.configureLoomAuthentication(
    sessionStorage: SessionManager,
    verifierCache: CodeVerifierCache,
) {
    autoLoadFromStorage = true
    autoSaveToStorage = true
    alwaysAutoRefresh = true
    enableLifecycleCallbacks = true
    sessionManager = sessionStorage
    codeVerifierCache = verifierCache
    flowType = FlowType.PKCE
    scheme = AuthenticationRedirect.SCHEME
    host = AuthenticationRedirect.HOST
    defaultRedirectUrl = AuthenticationRedirect.URL
}
