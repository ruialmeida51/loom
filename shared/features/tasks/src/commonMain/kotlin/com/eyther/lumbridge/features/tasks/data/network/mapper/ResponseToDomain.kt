package com.eyther.lumbridge.features.tasks.data.network.mapper

import com.eyther.lumbridge.features.tasks.data.network.model.response.TaskResponse
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import kotlin.time.Instant

internal fun TaskResponse.toDomain(): TaskDomain =
    TaskDomain(
        id = id,
        title = title,
        dueAt = dueAt?.let { Instant.parse(it) },
        recurrence =
            when (recurrence) {
                "none" -> TaskRecurrenceDomain.NONE
                "daily" -> TaskRecurrenceDomain.DAILY
                "weekly" -> TaskRecurrenceDomain.WEEKLY
                else -> throw IllegalArgumentException("Unknown task recurrence")
            },
        completed = completed,
        notificationEnabled = notificationEnabled,
        assigneeId = assigneeId,
        rotation = rotation,
    )
