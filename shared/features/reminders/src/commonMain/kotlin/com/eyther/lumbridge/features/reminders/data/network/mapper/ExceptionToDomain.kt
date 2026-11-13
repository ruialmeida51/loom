package com.eyther.lumbridge.features.reminders.data.network.mapper

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.reminders.domain.errors.ReminderError
import io.github.jan.supabase.postgrest.exception.PostgrestRestException

internal fun Exception.toReminderError(): AppError {
    if (this !is PostgrestRestException) return toAppError()

    if (code != "P0001") return toAppError()

    return when (error) {
        "Reminder access denied." -> ReminderError.AccessDenied(cause = this)
        else -> toAppError()
    }
}
