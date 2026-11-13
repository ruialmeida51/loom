package com.eyther.lumbridge.features.reminders.domain.usecase

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import kotlinx.datetime.*

class RemoveReminderUseCase(private val source: ReminderDataSource) {
    suspend operator fun invoke(id: String) = source.remove(id)
}
