package com.eyther.lumbridge.features.reminders.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain

class CompleteReminderUseCase(
    private val reminderDataSource: ReminderDataSource,
) {

    suspend operator fun invoke(reminder: ReminderDomain): AppResult<Unit> = reminderDataSource.completeReminder(reminder)
}
