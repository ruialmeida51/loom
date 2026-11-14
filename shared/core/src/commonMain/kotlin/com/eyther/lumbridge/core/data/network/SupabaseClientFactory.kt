package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.data.network.config.LoomBuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

fun createLoomSupabaseClient(sessionStorage: SessionManager): SupabaseClient? {
    if (LoomBuildConfig.supabaseUrl.isBlank() || LoomBuildConfig.supabasePublishableKey.isBlank()) return null

    return createSupabaseClient(
        supabaseUrl = LoomBuildConfig.supabaseUrl,
        supabaseKey = LoomBuildConfig.supabasePublishableKey,
    ) {
        install(Auth) { sessionManager = sessionStorage }
        install(Postgrest)
        install(Realtime)
        install(Functions)
    }
}
