package com.eyther.lumbridge.features.tasks.domain

import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class TaskTimeZoneTest {
    @Test
    fun dailyRepeatPreservesWallClockAcrossDaylightSaving() {
        val item =
            TaskDomain(
                id = "id",
                title = "Plants",
                dueAt = Instant.parse("2026-03-28T09:00:00Z"),
                recurrence = TaskRecurrenceDomain.DAILY,
                completed = false,
            )
        val next = assertNotNull(item.nextDue("Europe/Lisbon"))
        assertEquals(Instant.parse("2026-03-29T08:00:00Z"), next)
        assertEquals(LocalTime(9, 0), next.toLocalDateTime(TimeZone.of("Europe/Lisbon")).time)
    }

    @Test
    fun undatedTaskHasNoAutomaticNextOccurrence() {
        val item =
            TaskDomain(
                id = "id",
                title = "Shelf",
                dueAt = null,
                recurrence = TaskRecurrenceDomain.NONE,
                completed = false,
            )
        assertNull(item.nextDue("Europe/Lisbon"))
    }
}
