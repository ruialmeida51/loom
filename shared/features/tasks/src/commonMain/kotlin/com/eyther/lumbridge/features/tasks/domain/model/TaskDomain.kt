package com.eyther.lumbridge.features.tasks.domain.model

import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class TaskDomain(
    val id: String,
    val title: String,
    val dueAt: Instant?,
    val recurrence: TaskRecurrenceDomain,
    val completed: Boolean,
    val notificationEnabled: Boolean = false,
) {
    /** A repeat stays at the household's wall-clock time across daylight-saving changes. */
    fun nextDue(zone: String): Instant? {
        val due = dueAt ?: return null
        val period = recurrence.period ?: return null
        val timeZone = TimeZone.of(zone)
        val local = due.toLocalDateTime(timeZone)
        return LocalDateTime(local.date.plus(period), local.time).toInstant(timeZone)
    }
}
