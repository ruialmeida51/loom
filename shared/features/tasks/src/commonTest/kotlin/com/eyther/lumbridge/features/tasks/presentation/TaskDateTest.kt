package com.eyther.lumbridge.features.tasks.presentation

import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.presentation.mapper.toDateUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.datetime.LocalDate

class TaskDateTest {
    @Test
    fun relativeDatesUseCalendarDaysInTheHouseholdZone() {
        val task =
            TaskDomain(
                id = "id",
                title = "Bins",
                dueAt = Instant.parse("2026-11-17T23:30:00Z"),
                recurrence = TaskRecurrenceDomain.DAILY,
                completed = false,
                notificationEnabled = true,
            )
        val lisbon =
            assertNotNull(
                task.toDateUi(today = LocalDate(2026, 11, 16), timeZone = "Europe/Lisbon"),
            )
        val tokyo =
            assertNotNull(task.toDateUi(today = LocalDate(2026, 11, 16), timeZone = "Asia/Tokyo"))
        assertEquals(1, lisbon.daysFromToday)
        assertEquals(2, tokyo.daysFromToday)
        assertEquals(18, tokyo.day)
        assertEquals("08:30", tokyo.time)
        assertNull(tokyo.year)
    }

    @Test
    fun overdueAndYearBoundariesRemainUnambiguous() {
        val task =
            TaskDomain(
                id = "id",
                title = "Rent",
                dueAt = Instant.parse("2026-12-31T23:30:00Z"),
                recurrence = TaskRecurrenceDomain.NONE,
                completed = false,
            )
        val date = assertNotNull(task.toDateUi(today = LocalDate(2027, 1, 2), timeZone = "UTC"))
        assertEquals(-2, date.daysFromToday)
        assertEquals(2026, date.year)
        assertEquals(12, date.month)
        assertNull(date.time, "A due date alone is not a notification time")
    }

    @Test
    fun undatedTasksNeverReceiveAnInventedTodayLabel() {
        val task =
            TaskDomain(
                id = "id",
                title = "Shelf",
                dueAt = null,
                recurrence = TaskRecurrenceDomain.NONE,
                completed = false,
            )
        assertNull(task.toDateUi(today = LocalDate(2026, 11, 17), timeZone = "Europe/Lisbon"))
    }
}
