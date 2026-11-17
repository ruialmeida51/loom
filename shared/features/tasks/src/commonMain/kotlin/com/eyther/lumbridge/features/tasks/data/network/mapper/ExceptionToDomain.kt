package com.eyther.lumbridge.features.tasks.data.network.mapper

import com.eyther.lumbridge.core.data.network.mapper.toAppError
import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.features.tasks.domain.errors.TaskError
import io.github.jan.supabase.postgrest.exception.PostgrestRestException

internal fun Exception.toTaskError(): AppError {
    if (this !is PostgrestRestException) return toAppError()

    if (code != "P0001") return toAppError()

    return when (error) {
        "Task access denied." -> TaskError.AccessDenied(cause = this)
        else -> toAppError()
    }
}
