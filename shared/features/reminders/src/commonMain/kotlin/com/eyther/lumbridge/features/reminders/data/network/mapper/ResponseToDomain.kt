package com.eyther.lumbridge.features.reminders.data.network.mapper

import com.eyther.lumbridge.features.reminders.data.network.model.response.ReminderResponse
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain

internal fun ReminderResponse.toDomain(): ReminderDomain = ReminderDomain(
    id = id,
    title = title,
    dueAt = dueAt,
    recurrence = recurrence,
    completed = completed,
)
