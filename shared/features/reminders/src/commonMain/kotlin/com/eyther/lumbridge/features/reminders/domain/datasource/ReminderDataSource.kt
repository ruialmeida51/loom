package com.eyther.lumbridge.features.reminders.domain.datasource

import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.*

interface ReminderDataSource {
    fun observe(): Flow<List<ReminderDomain>>

    suspend fun add(reminder: ReminderDomain): AppResult<Unit>

    suspend fun complete(reminder: ReminderDomain): AppResult<Unit>

    suspend fun remove(id: String): AppResult<Unit>
}
