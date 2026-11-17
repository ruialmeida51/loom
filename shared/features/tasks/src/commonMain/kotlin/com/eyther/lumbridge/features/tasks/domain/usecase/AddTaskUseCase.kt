package com.eyther.lumbridge.features.tasks.domain.usecase

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.errors.TaskError
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class AddTaskUseCase(
    private val taskDataSource: TaskDataSource,
    private val context: HouseholdContextDomain,
) {

    suspend operator fun invoke(
        title: String,
        date: LocalDate?,
        time: LocalTime,
        recurrence: TaskRecurrenceDomain,
        notificationEnabled: Boolean = false,
    ): AppResult<TaskDomain> {
        if (title.trim().length !in 1..MAX_TITLE_LENGTH)
            return AppResult.Failure(
                TaskError.InvalidTitle(),
            )
        if (date == null && (recurrence != TaskRecurrenceDomain.NONE || notificationEnabled))
            return AppResult.Failure(TaskError.DateRequired())
        val instant =
            try {
                date?.let {
                    LocalDateTime(date = it, time = time).toInstant(TimeZone.of(context.timeZone))
                }
            } catch (_: IllegalArgumentException) {
                return AppResult.Failure(TaskError.InvalidDateTime())
            }
        val task =
            TaskDomain(
                id = newId(),
                title = title.trim(),
                dueAt = instant,
                recurrence = recurrence,
                completed = false,
                notificationEnabled = notificationEnabled,
            )
        return when (val result = taskDataSource.addTask(task)) {
            is AppResult.Success -> AppResult.Success(task)
            is AppResult.Failure -> result
        }
    }
}

private const val MAX_TITLE_LENGTH = 160
