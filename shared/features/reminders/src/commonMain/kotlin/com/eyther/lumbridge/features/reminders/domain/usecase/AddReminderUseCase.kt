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
            return AppResult.Failure(
                "reminders.message_give_your_reminder_a_title_up_to_160_characters",
            )
        if (recurrence !in listOf("none", "daily", "weekly"))
            return AppResult.Failure("reminders.message_choose_a_repeat_option")
        val instant =
            try {
                LocalDateTime.parse("${date}T${time}:00").toInstant(TimeZone.of(context.timeZone))
            } catch (_: IllegalArgumentException) {
                return AppResult.Failure("reminders.message_use_a_date_like_2026_11_13_and_a")
            }
        val reminder = ReminderDomain(newId(), title.trim(), instant.toString(), recurrence, false)
        return when (val result = source.add(reminder)) {
            is AppResult.Success -> AppResult.Success(reminder)
            is AppResult.Failure -> result
        }
    }
}
