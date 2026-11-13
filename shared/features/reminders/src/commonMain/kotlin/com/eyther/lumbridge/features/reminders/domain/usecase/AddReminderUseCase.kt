package com.eyther.lumbridge.features.reminders.domain.usecase

import com.eyther.lumbridge.core.domain.AppResult
import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.datetime.*

class AddReminderUseCase(
    private val source: ReminderDataSource,
    private val context: HouseholdContextDomain,
) {
    suspend operator fun invoke(
        title: String,
        date: String,
        time: String,
        recurrence: String,
    ): AppResult<ReminderDomain> {
        if (title.trim().length !in 1..160)
            return AppResult.Failure("Give your reminder a title, up to 160 characters.")
        if (recurrence !in listOf("none", "daily", "weekly"))
            return AppResult.Failure("Choose a repeat option.")
        val instant =
            try {
                LocalDateTime.parse("${date}T${time}:00").toInstant(TimeZone.of(context.timeZone))
            } catch (_: IllegalArgumentException) {
                return AppResult.Failure("Use a date like 2026-11-13 and a time like 18:30.")
            }
        val reminder = ReminderDomain(newId(), title.trim(), instant.toString(), recurrence, false)
        return when (val result = source.add(reminder)) {
            is AppResult.Success -> AppResult.Success(reminder)
            is AppResult.Failure -> result
        }
    }
}
