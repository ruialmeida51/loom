package com.eyther.lumbridge.features.reminders.presentation

import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.presentation.mapper.toDateUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.LocalDate

class ReminderDateTest {
    @Test
    fun relativeDatesUseCalendarDaysInTheHouseholdZone() {
        val reminder = ReminderDomain("id", "Bins", "2026-11-17T23:30:00Z", "daily", false)
        val lisbon = reminder.toDateUi(today = LocalDate(2026, 11, 16), timeZone = "Europe/Lisbon")
        val tokyo = reminder.toDateUi(today = LocalDate(2026, 11, 16), timeZone = "Asia/Tokyo")
        assertEquals(1, lisbon.daysFromToday)
        assertEquals(2, tokyo.daysFromToday)
        assertEquals(18, tokyo.day)
        assertEquals("08:30", tokyo.time)
        assertNull(tokyo.year)
    }

    @Test
    fun overdueAndYearBoundariesRemainUnambiguous() {
        val reminder = ReminderDomain("id", "Rent", "2026-12-31T23:30:00Z", "none", false)
        val nextYear = reminder.toDateUi(today = LocalDate(2027, 1, 2), timeZone = "UTC")
        assertEquals(-2, nextYear.daysFromToday)
        assertEquals(2026, nextYear.year)
        assertEquals(12, nextYear.month)
    }
}
