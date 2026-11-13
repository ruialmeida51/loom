package com.eyther.lumbridge.features.expenses.domain

import com.eyther.lumbridge.features.expenses.domain.model.*
import kotlin.test.*
import kotlinx.datetime.LocalDate

class MonthlyRecurrenceTest {
    @Test
    fun monthEndAnchorRecoversAfterShortMonths() {
        val feb = MonthlyRecurrenceDomain.nextAfter(LocalDate.parse("2028-01-31"), 31)
        assertEquals("2028-02-29", feb.toString())
        assertEquals("2028-03-31", MonthlyRecurrenceDomain.nextAfter(feb, 31).toString())
    }

    @Test
    fun resumingSkipsPausedOccurrencesAndPreservesFutureSchedules() {
        assertEquals(
            "2026-11-20",
            MonthlyRecurrenceDomain.resumeOn(
                    LocalDate.parse("2026-07-20"),
                    LocalDate.parse("2026-11-13"),
                    20,
                )
                .toString(),
        )
        assertEquals(
            "2026-12-10",
            MonthlyRecurrenceDomain.resumeOn(
                    LocalDate.parse("2026-07-10"),
                    LocalDate.parse("2026-11-13"),
                    10,
                )
                .toString(),
        )
        assertEquals(
            "2027-01-31",
            MonthlyRecurrenceDomain.resumeOn(
                    LocalDate.parse("2027-01-31"),
                    LocalDate.parse("2026-11-13"),
                    31,
                )
                .toString(),
        )
    }

    @Test
    fun scheduleManagementBelongsToPayerCreatorOrOwner() {
        val rule =
            RecurringExpenseDomain(
                "id",
                "Rent",
                100,
                "payer",
                "Bills",
                "2026-12-01",
                1,
                true,
                false,
                "creator",
                1,
                emptyMap(),
            )
        assertTrue(rule.canManage("payer", false))
        assertTrue(rule.canManage("creator", false))
        assertTrue(rule.canManage("other", true))
        assertFalse(rule.canManage("other", false))
        assertFalse(rule.copy(cancelled = true).canManage("payer", true))
    }
}
