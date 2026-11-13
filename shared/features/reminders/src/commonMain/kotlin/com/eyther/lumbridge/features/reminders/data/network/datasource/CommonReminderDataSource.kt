package com.eyther.lumbridge.features.reminders.data.network.datasource

import com.eyther.lumbridge.core.data.SupabaseAccess
import com.eyther.lumbridge.core.data.safeApiCall
import com.eyther.lumbridge.core.data.selectHouseholdRows
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
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
    private val sample =
        MutableStateFlow(
            listOf(
                ReminderDomain(
                    newId(),
                    "Water the plants",
                    (Clock.System.now() + 2.hours).toString(),
                    "weekly",
                    false,
                ),
                ReminderDomain(
                    newId(),
                    "Take the recycling out",
                    (Clock.System.now() + 7.hours).toString(),
                    "weekly",
                    false,
                ),
                ReminderDomain(
                    newId(),
                    "Book the boiler service",
                    (Clock.System.now() + 30.hours).toString(),
                    "none",
                    false,
                ),
            ),
        )

    override fun observe(): Flow<List<ReminderDomain>> =
        if (context.demo) sample
        else
            access.watch("reminders", context.id) {
                access.selectHouseholdRows<ReminderDto>("reminders", context.id).map {
                    it.toDomain()
                }
            }

    override suspend fun add(reminder: ReminderDomain) = safeApiCall {
        if (context.demo) sample.update { it + reminder }
        else
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
        if (context.demo)
            sample.update { items ->
                items.map {
                    if (it.id != reminder.id || it.dueAt != reminder.dueAt) it
                    else if (it.recurrence != "none" && !it.completed)
                        it.copy(dueAt = it.nextDue(context.timeZone))
                    else it.copy(completed = !it.completed)
                }
            }
        else
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
        if (context.demo) sample.update { items -> items.filterNot { it.id == id } }
        else
            access.requireClient().from("reminders").delete {
                filter {
                    eq("id", id)
                    eq("household_id", context.id)
                }
            }
        Unit
    }
}
