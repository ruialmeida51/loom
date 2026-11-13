package com.eyther.lumbridge.features.reminders.domain.usecase

import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import kotlinx.datetime.*

class ObserveRemindersUseCase(private val source: ReminderDataSource) {
    operator fun invoke() = source.observe()
}
