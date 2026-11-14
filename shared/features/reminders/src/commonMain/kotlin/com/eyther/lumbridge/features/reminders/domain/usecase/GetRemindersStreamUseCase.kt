package com.eyther.lumbridge.features.reminders.domain.usecase

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.coroutines.flow.Flow

class GetRemindersStreamUseCase(
    private val reminderDataSource: ReminderDataSource,
) {

    operator fun invoke(): Flow<AppResult<List<ReminderDomain>>> = reminderDataSource.stream()
}
