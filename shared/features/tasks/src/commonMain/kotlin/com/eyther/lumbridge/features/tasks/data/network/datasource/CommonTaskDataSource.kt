package com.eyther.lumbridge.features.tasks.data.network.datasource

import com.eyther.lumbridge.core.data.network.SupabaseAccess
import com.eyther.lumbridge.core.data.network.asApiResult
import com.eyther.lumbridge.core.data.network.safeApiCall
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.data.network.selectHouseholdRows
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.data.network.mapper.toDomain
import com.eyther.lumbridge.features.tasks.data.network.mapper.toRequest
import com.eyther.lumbridge.features.tasks.data.network.mapper.toTaskError
import com.eyther.lumbridge.features.tasks.data.network.model.request.AssignTaskRequest
import com.eyther.lumbridge.features.tasks.data.network.model.request.CompleteTaskRequest
import com.eyther.lumbridge.features.tasks.data.network.model.response.TaskResponse
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.Flow

class CommonTaskDataSource(
    private val access: SupabaseAccess,
    private val context: HouseholdContextDomain,
) : TaskDataSource {

    override fun stream(): Flow<AppResult<List<TaskDomain>>> {
        return access
            .watch {
                access.selectHouseholdRows<TaskResponse>("tasks", context.id).map {
                    it.toDomain()
                }
            }
            .asApiResult(mapError = Exception::toTaskError, retryTransientFailures = true)
    }

    override suspend fun addTask(task: TaskDomain): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toTaskError,
        ) {
            val request = task.toRequest(householdId = context.id, userId = context.userId)

            access.write { client ->
                client.from("tasks").insert(request)
            }
            Unit
        }
    }

    override suspend fun completeTask(task: TaskDomain): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toTaskError,
        ) {
            val request =
                CompleteTaskRequest(
                    taskId = task.id,
                    completed = !task.completed,
                    expectedDueAt = task.dueAt?.toString(),
                )

            access.write { client ->
                client.postgrest.rpc("complete_task", request)
            }
            Unit
        }
    }

    override suspend fun removeTask(id: String): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toTaskError,
        ) {
            access.write { client ->
                client.from("tasks").delete {
                    filter {
                        eq("id", id)
                        eq("household_id", context.id)
                    }
                }
            }
            Unit
        }
    }

    override suspend fun assignTask(task: TaskDomain): AppResult<Unit> {
        return safeApiCall(
            mapError = Exception::toTaskError,
        ) {
            access.write { client ->
                client.from("tasks").update(
                    AssignTaskRequest(assigneeId = task.assigneeId, rotation = task.rotation),
                ) {
                    filter {
                        eq("id", task.id)
                        eq("household_id", context.id)
                    }
                }
            }
            Unit
        }
    }
}
