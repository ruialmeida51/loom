package com.eyther.lumbridge.features.reminders.presentation.mapper

import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.presentation.model.ReminderDateUi
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

fun ReminderDomain.toDateUi(today: LocalDate, timeZone: String): ReminderDateUi {
    val local = Instant.parse(dueAt).toLocalDateTime(TimeZone.of(timeZone))
    return ReminderDateUi(
        daysFromToday = today.daysUntil(local.date),
        day = local.day,
        month = local.month.ordinal + 1,
        year = local.year.takeIf { it != today.year },
        time =
            "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}",
    )
}
