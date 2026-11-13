package com.eyther.lumbridge.features.reminders.domain.datasource

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.coroutines.flow.Flow

interface ReminderDataSource {
    fun stream(): Flow<AppResult<List<ReminderDomain>>>

    suspend fun addReminder(reminder: ReminderDomain): AppResult<Unit>

    suspend fun completeReminder(reminder: ReminderDomain): AppResult<Unit>

    suspend fun removeReminder(id: String): AppResult<Unit>
}
