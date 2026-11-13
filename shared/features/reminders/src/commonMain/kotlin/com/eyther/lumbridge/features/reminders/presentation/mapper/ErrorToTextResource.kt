package com.eyther.lumbridge.features.reminders.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.reminders.domain.errors.ReminderError
import com.eyther.lumbridge.features.reminders.generated.resources.Res
import com.eyther.lumbridge.features.reminders.generated.resources.error_access_denied
import com.eyther.lumbridge.features.reminders.generated.resources.error_invalid_date_time
import com.eyther.lumbridge.features.reminders.generated.resources.error_invalid_recurrence
import com.eyther.lumbridge.features.reminders.generated.resources.error_invalid_title

fun AppError.toRemindersTextResource(): TextResource = when (this) {
    is ReminderError.InvalidTitle -> TextResource.Resource(Res.string.error_invalid_title)
    is ReminderError.InvalidRecurrence -> TextResource.Resource(Res.string.error_invalid_recurrence)
    is ReminderError.InvalidDateTime -> TextResource.Resource(Res.string.error_invalid_date_time)
    is ReminderError.AccessDenied -> TextResource.Resource(Res.string.error_access_denied)
    else -> toTextResource()
}
