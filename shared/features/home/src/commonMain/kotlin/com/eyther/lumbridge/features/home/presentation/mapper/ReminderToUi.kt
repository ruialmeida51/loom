package com.eyther.lumbridge.features.home.presentation.mapper

import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun ReminderDomain.toHomeUi(today: String, timeZone: String): HomeReminderUi {
    val local = Instant.parse(dueAt).toLocalDateTime(TimeZone.of(timeZone))
    val date = local.date.toString()
    val time =
        "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
    return HomeReminderUi(
        id = id,
        title = title,
        due = if (date == today) time else "$date · $time",
        overdue = today.isNotEmpty() && date < today,
    )
}
