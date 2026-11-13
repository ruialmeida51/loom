package com.eyther.lumbridge.features.reminders.domain.usecase

import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.datetime.*

class CompleteReminderUseCase(private val source: ReminderDataSource) {
    suspend operator fun invoke(reminder: ReminderDomain) = source.complete(reminder)
}
