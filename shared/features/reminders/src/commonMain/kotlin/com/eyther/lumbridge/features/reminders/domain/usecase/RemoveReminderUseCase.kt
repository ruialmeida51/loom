package com.eyther.lumbridge.features.reminders.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource

class RemoveReminderUseCase(
    private val reminderDataSource: ReminderDataSource,
) {

    suspend operator fun invoke(id: String): AppResult<Unit> = reminderDataSource.removeReminder(id)
}
