package com.eyther.lumbridge.core.data.network

import com.eyther.lumbridge.core.data.network.config.LoomBuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseExperimental
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

@OptIn(SupabaseExperimental::class)
fun createLoomSupabaseClient(configureAuthentication: AuthConfig.() -> Unit): SupabaseClient? {
    if (LoomBuildConfig.supabaseUrl.isBlank() || LoomBuildConfig.supabasePublishableKey.isBlank())
        return null

    return createSupabaseClient(
        supabaseUrl = LoomBuildConfig.supabaseUrl,
        supabaseKey = LoomBuildConfig.supabasePublishableKey,
    ) {
        install(Auth, configureAuthentication)
        install(Postgrest) { requireValidSession = true }
        install(Storage) { requireValidSession = true }
        install(Functions) { requireValidSession = true }
    }
}
