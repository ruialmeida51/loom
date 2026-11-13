package com.eyther.lumbridge.features.reminders.domain.usecase

import com.eyther.lumbridge.core.domain.newId
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.errors.ReminderError
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

class AddReminderUseCase(
    private val reminderDataSource: ReminderDataSource,
    private val context: HouseholdContextDomain,
) {

    suspend operator fun invoke(
        title: String,
        date: String,
        time: String,
        recurrence: String,
    ): AppResult<ReminderDomain> {
        if (title.trim().length !in 1..160)
            return AppResult.Failure(
                ReminderError.InvalidTitle(),
            )
        if (recurrence !in listOf("none", "daily", "weekly"))
            return AppResult.Failure(ReminderError.InvalidRecurrence())
        val instant =
            try {
                LocalDateTime.parse("${date}T${time}:00").toInstant(TimeZone.of(context.timeZone))
            } catch (_: IllegalArgumentException) {
                return AppResult.Failure(ReminderError.InvalidDateTime())
            }
        val reminder = ReminderDomain(
            id = newId(),
            title = title.trim(),
            dueAt = instant.toString(),
            recurrence = recurrence,
            completed = false,
        )
        return when (val result = reminderDataSource.addReminder(reminder)) {
            is AppResult.Success -> AppResult.Success(reminder)
            is AppResult.Failure -> result
        }
    }
}
