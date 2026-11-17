package com.eyther.lumbridge.features.tasks.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

internal class TaskCreationFixtureDataSource : TaskDataSource {
    var created: TaskDomain? = null
        private set

    override fun stream(): Flow<AppResult<List<TaskDomain>>> =
        flowOf(AppResult.Success(emptyList()))

    override suspend fun addTask(task: TaskDomain): AppResult<Unit> {
        created = task
        return AppResult.Success(Unit)
    }

    override suspend fun completeTask(task: TaskDomain): AppResult<Unit> = AppResult.Success(Unit)

    override suspend fun removeTask(id: String): AppResult<Unit> = AppResult.Success(Unit)
}
