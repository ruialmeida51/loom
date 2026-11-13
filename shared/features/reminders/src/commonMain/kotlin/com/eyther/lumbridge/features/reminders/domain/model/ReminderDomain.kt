package com.eyther.lumbridge.features.reminders.domain.model

import kotlin.time.Instant
import kotlinx.datetime.*

data class ReminderDomain(
    val id: String,
    val title: String,
    val dueAt: String,
    val recurrence: String,
    val completed: Boolean,
) {
    fun dueLabel(zone: String): String {
        val local = Instant.parse(dueAt).toLocalDateTime(TimeZone.of(zone))
        return "${local.date} · ${local.hour.toString().padStart(2,'0')}:${local.minute.toString().padStart(2,'0')}"
    }

    fun nextDue(zone: String): String {
        val local = Instant.parse(dueAt).toLocalDateTime(TimeZone.of(zone))
        val date = local.date.plus(DatePeriod(days = if (recurrence == "weekly") 7 else 1))
        return LocalDateTime(date, local.time).toInstant(TimeZone.of(zone)).toString()
    }
}
