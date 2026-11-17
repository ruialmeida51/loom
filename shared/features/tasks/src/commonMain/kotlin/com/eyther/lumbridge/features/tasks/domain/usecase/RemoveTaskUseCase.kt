package com.eyther.lumbridge.features.tasks.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource

class RemoveTaskUseCase(
    private val taskDataSource: TaskDataSource,
) {

    suspend operator fun invoke(id: String): AppResult<Unit> = taskDataSource.removeTask(id)
}
