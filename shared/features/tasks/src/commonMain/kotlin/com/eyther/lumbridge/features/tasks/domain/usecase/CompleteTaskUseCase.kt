package com.eyther.lumbridge.features.tasks.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain

class CompleteTaskUseCase(
    private val taskDataSource: TaskDataSource,
) {

    suspend operator fun invoke(task: TaskDomain): AppResult<Unit> =
        taskDataSource.completeTask(task)
}
