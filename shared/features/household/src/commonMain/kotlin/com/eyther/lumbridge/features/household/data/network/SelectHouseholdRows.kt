package com.eyther.lumbridge.features.household.data.network

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

/**
 * Keyset pagination prevents a Data API row limit from silently truncating household balances.
 * [where] narrows the rows read (for example, only rows that are not removed).
 */
suspend inline fun <reified T> SupabaseAccess.selectHouseholdRows(
    table: String,
    householdId: String,
    crossinline where: PostgrestFilterBuilder.() -> Unit = {},
): List<T> =
    withContext(Dispatchers.IO) {
        val result = mutableListOf<T>()
        val json = Json { ignoreUnknownKeys = true }
        var cursor: String? = null
        do {
            val page =
                requireClient()
                    .from(table)
                    .select {
                        filter {
                            eq("household_id", householdId)
                            where()
                            cursor?.let { gt("id", it) }
                        }
                        order("id", Order.ASCENDING)
                        limit(500)
                    }
                    .decodeList<JsonObject>()
            result += page.map { json.decodeFromJsonElement<T>(it) }
            cursor = page.lastOrNull()?.get("id")?.jsonPrimitive?.contentOrNull
        } while (page.size == 500 && cursor != null)
        result
    }
