package com.eyther.lumbridge.features.tasks.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.errors.TaskError
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain

/** Gives [task] to [assigneeId] (or anyone when null); with [takesTurns], everyone rotates. */
class AssignTaskUseCase(private val taskDataSource: TaskDataSource) {
    suspend operator fun invoke(
        task: TaskDomain,
        assigneeId: String?,
        takesTurns: Boolean,
        memberIds: List<String>,
    ): AppResult<TaskDomain> {
        val assigned =
            task.copy(
                assigneeId = assigneeId,
                rotation = TaskDomain.rotationFor(assigneeId, takesTurns, memberIds),
            )
        if (!assigned.hasValidPeople()) return AppResult.Failure(TaskError.InvalidAssignee())
        return when (val result = taskDataSource.assignTask(assigned)) {
            is AppResult.Success -> AppResult.Success(assigned)
            is AppResult.Failure -> result
        }
    }
}
