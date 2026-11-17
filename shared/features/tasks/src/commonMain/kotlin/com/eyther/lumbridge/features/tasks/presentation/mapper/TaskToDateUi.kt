package com.eyther.lumbridge.features.tasks.presentation.mapper

import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.presentation.model.TaskDateUi
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

fun TaskDomain.toDateUi(today: LocalDate, timeZone: String): TaskDateUi? {
    val local = dueAt?.toLocalDateTime(TimeZone.of(timeZone)) ?: return null
    return TaskDateUi(
        daysFromToday = today.daysUntil(local.date),
        day = local.day,
        month = local.month.ordinal + 1,
        year = local.year.takeIf { it != today.year },
        time =
            if (notificationEnabled)
                "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
            else null,
    )
}
