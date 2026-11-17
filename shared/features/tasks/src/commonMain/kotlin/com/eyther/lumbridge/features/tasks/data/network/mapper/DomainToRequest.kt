package com.eyther.lumbridge.features.tasks.data.network.mapper

import com.eyther.lumbridge.features.tasks.data.network.model.request.CreateTaskRequest
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain

internal fun TaskDomain.toRequest(
    householdId: String,
    userId: String,
): CreateTaskRequest =
    CreateTaskRequest(
        id = id,
        householdId = householdId,
        title = title,
        dueAt = dueAt?.toString(),
        recurrence =
            when (recurrence) {
                TaskRecurrenceDomain.NONE -> "none"
                TaskRecurrenceDomain.DAILY -> "daily"
                TaskRecurrenceDomain.WEEKLY -> "weekly"
            },
        completed = completed,
        createdBy = userId,
        notificationEnabled = notificationEnabled,
    )
