package com.eyther.lumbridge.features.reminders.data.network.datasource

import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.data.safeApiCall
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
private data class ReminderDto(
    @SerialName("id") val id: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("title") val title: String,
    @SerialName("due_at") val dueAt: String,
    @SerialName("recurrence") val recurrence: String,
    @SerialName("completed") val completed: Boolean,
    @SerialName("created_by") val createdBy: String,
) {
    fun toDomain() = ReminderDomain(id, title, dueAt, recurrence, completed)
}

class CommonReminderDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ReminderDataSource {
    override fun observe(): Flow<List<ReminderDomain>> =
        access.watch("reminders", context.id) {
            access.selectHouseholdRows<ReminderDto>("reminders", context.id).map {
                it.toDomain()
            }
        }

    override suspend fun add(reminder: ReminderDomain) = safeApiCall {
        access
            .requireClient()
            .from("reminders")
            .insert(
                ReminderDto(
                    reminder.id,
                    context.id,
                    reminder.title,
                    reminder.dueAt,
                    reminder.recurrence,
                    reminder.completed,
                    context.userId,
                ),
            )
        Unit
    }

    override suspend fun complete(reminder: ReminderDomain) = safeApiCall {
        access
            .requireClient()
            .postgrest
            .rpc(
                "complete_reminder",
                buildJsonObject {
                    put("p_reminder_id", reminder.id)
                    put("p_completed", !reminder.completed)
                    put("p_expected_due_at", reminder.dueAt)
                },
            )
        Unit
    }

    override suspend fun remove(id: String) = safeApiCall {
        access.requireClient().from("reminders").delete {
            filter {
                eq("id", id)
                eq("household_id", context.id)
            }
        }
        Unit
    }
}
