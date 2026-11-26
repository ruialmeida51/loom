package com.eyther.lumbridge.features.household.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.domain.features.Feature
import com.eyther.lumbridge.core.domain.features.FeatureGate
import com.eyther.lumbridge.features.household.data.network.model.response.HouseholdFeatureResponse
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Reads the household's switched-off features once per household graph. */
class CommonFeatureGate(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : FeatureGate {
    private val lock = Mutex()
    private var disabled: Set<String>? = null

    override suspend fun isEnabled(feature: Feature): Boolean =
        feature.key !in lock.withLock { disabled ?: load().also { disabled = it } }

    private suspend fun load(): Set<String> =
        runCatching {
                access
                    .requireClient()
                    .from("household_features")
                    .select {
                        filter {
                            eq("household_id", context.id)
                            eq("enabled", false)
                        }
                    }
                    .decodeList<HouseholdFeatureResponse>()
                    .map { it.feature }
                    .toSet()
            }
            .getOrDefault(emptySet())
}

