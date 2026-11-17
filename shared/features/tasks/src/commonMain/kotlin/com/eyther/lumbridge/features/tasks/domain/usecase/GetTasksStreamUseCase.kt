package com.eyther.lumbridge.features.tasks.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import kotlinx.coroutines.flow.Flow

class GetTasksStreamUseCase(
    private val taskDataSource: TaskDataSource,
) {

    operator fun invoke(): Flow<AppResult<List<TaskDomain>>> = taskDataSource.stream()
}
