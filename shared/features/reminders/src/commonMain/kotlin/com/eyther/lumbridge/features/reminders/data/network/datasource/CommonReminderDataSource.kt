package com.eyther.lumbridge.features.reminders.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.data.network.mapper.toDomain
import com.eyther.lumbridge.features.reminders.data.network.mapper.toReminderError
import com.eyther.lumbridge.features.reminders.data.network.mapper.toRequest
import com.eyther.lumbridge.features.reminders.data.network.model.request.CompleteReminderRequest
import com.eyther.lumbridge.features.reminders.data.network.model.response.ReminderResponse
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow

class CommonReminderDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : ReminderDataSource {

    override fun stream(): Flow<AppResult<List<ReminderDomain>>> {
        return access.watch("reminders", context.id) {
            access.selectHouseholdRows<ReminderResponse>("reminders", context.id).map { it.toDomain() }
        }.asApiResult(mapError = Exception::toReminderError)
    }

    override suspend fun addReminder(reminder: ReminderDomain): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toReminderError,
        ) {
            val request = reminder.toRequest(householdId = context.id, userId = context.userId)

            access.requireClient().from("reminders").insert(request)
            Unit
        }
    }

    override suspend fun completeReminder(reminder: ReminderDomain): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toReminderError,
        ) {
            val request = CompleteReminderRequest(
                reminderId = reminder.id,
                completed = !reminder.completed,
                expectedDueAt = reminder.dueAt,
            )

            access.requireClient().postgrest.rpc("complete_reminder", request)
            Unit
        }
    }

    override suspend fun removeReminder(id: String): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toReminderError,
        ) {
            access.requireClient().from("reminders").delete {
                filter {
                    eq("id", id)
                    eq("household_id", context.id)
                }
            }
            Unit
        }
    }
}
