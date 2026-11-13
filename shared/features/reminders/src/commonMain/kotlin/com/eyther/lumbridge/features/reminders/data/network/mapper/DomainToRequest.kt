package com.eyther.lumbridge.features.reminders.data.network.mapper

import com.eyther.lumbridge.features.reminders.data.network.model.request.CreateReminderRequest
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain

internal fun ReminderDomain.toRequest(
    householdId: String,
    userId: String,
): CreateReminderRequest = CreateReminderRequest(
    id = id,
    householdId = householdId,
    title = title,
    dueAt = dueAt,
    recurrence = recurrence,
    completed = completed,
    createdBy = userId,
)
