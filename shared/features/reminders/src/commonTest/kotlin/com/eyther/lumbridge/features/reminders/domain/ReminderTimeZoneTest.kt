package com.eyther.lumbridge.features.reminders.domain

import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlin.test.*

class ReminderTimeZoneTest {
    @Test
    fun dailyRepeatPreservesWallClockAcrossDaylightSaving() {
        val item = ReminderDomain("id", "Plants", "2026-03-28T09:00:00Z", "daily", false)
        assertEquals("2026-03-29T08:00:00Z", item.nextDue("Europe/Lisbon"))
        assertEquals(
            "2026-03-29 · 09:00",
            item.copy(dueAt = item.nextDue("Europe/Lisbon")).dueLabel("Europe/Lisbon"),
        )
    }
}
