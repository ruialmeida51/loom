package com.eyther.lumbridge.features.tasks.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.presentation.mapper.toTextResource
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.tasks.domain.errors.TaskError
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.error_access_denied
import com.eyther.lumbridge.features.tasks.generated.resources.error_invalid_date_time
import com.eyther.lumbridge.features.tasks.generated.resources.error_invalid_title
import com.eyther.lumbridge.features.tasks.generated.resources.task_date_required

fun AppError.toTasksTextResource(): TextResource =
    when (this) {
        is TaskError.InvalidTitle -> TextResource.Resource(Res.string.error_invalid_title)
        is TaskError.InvalidDateTime -> TextResource.Resource(Res.string.error_invalid_date_time)
        is TaskError.DateRequired -> TextResource.Resource(Res.string.task_date_required)
        is TaskError.AccessDenied -> TextResource.Resource(Res.string.error_access_denied)
        else -> toTextResource()
    }
