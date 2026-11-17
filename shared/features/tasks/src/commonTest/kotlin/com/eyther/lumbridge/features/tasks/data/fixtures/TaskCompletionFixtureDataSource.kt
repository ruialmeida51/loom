package com.eyther.lumbridge.features.tasks.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import kotlin.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal class TaskCompletionFixtureDataSource : TaskDataSource {
    val task =
        TaskDomain(
            "daily",
            "Take out recycling",
            Instant.parse("2026-11-16T18:00:00Z"),
            TaskRecurrenceDomain.DAILY,
            false,
        )
    val items = MutableStateFlow<AppResult<List<TaskDomain>>>(AppResult.Success(listOf(task)))
    val completion = CompletableDeferred<AppResult<Unit>>()
    var calls = 0

    override fun stream() = items

    override suspend fun addTask(task: TaskDomain): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun completeTask(task: TaskDomain): AppResult<Unit> {
        calls++
        return completion.await()
    }

    override suspend fun removeTask(id: String): AppResult<Unit> = AppResult.Success(Unit)
}
