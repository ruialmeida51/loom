package com.eyther.lumbridge.features.tasks.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import kotlinx.coroutines.flow.Flow

interface TaskDataSource {
    fun stream(): Flow<AppResult<List<TaskDomain>>>

    suspend fun addTask(task: TaskDomain): AppResult<Unit>

    suspend fun completeTask(task: TaskDomain): AppResult<Unit>

    suspend fun removeTask(id: String): AppResult<Unit>
}
