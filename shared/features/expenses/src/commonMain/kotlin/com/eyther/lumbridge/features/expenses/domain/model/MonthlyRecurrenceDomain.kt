package com.eyther.lumbridge.features.expenses.domain.model

import kotlinx.datetime.*

object MonthlyRecurrenceDomain {
    fun nextAfter(date: LocalDate, anchorDay: Int): LocalDate {
        require(anchorDay in 1..31)
        val next = LocalDate(date.year, date.month, 1).plus(DatePeriod(months = 1))
        val last = next.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
        return LocalDate(next.year, next.month, minOf(anchorDay, last.day))
    }

    fun resumeOn(nextOn: LocalDate, today: LocalDate, anchorDay: Int): LocalDate {
        if (nextOn > today) return nextOn
        val start = LocalDate(today.year, today.month, 1)
        val end = start.plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
        val candidate = LocalDate(today.year, today.month, minOf(anchorDay, end.day))
        return if (candidate > today) candidate else nextAfter(today, anchorDay)
    }
}
