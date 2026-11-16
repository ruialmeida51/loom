package com.eyther.lumbridge.features.reminders.data.fixtures

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow

internal class ReminderCompletionFixtureDataSource : ReminderDataSource {
    val reminder =
        ReminderDomain("daily", "Take out recycling", "2026-11-16T18:00:00Z", "daily", false)
    val items =
        MutableStateFlow<AppResult<List<ReminderDomain>>>(AppResult.Success(listOf(reminder)))
    val completion = CompletableDeferred<AppResult<Unit>>()
    var calls = 0

    override fun stream() = items

    override suspend fun addReminder(reminder: ReminderDomain): AppResult<Unit> =
        AppResult.Success(Unit)

    override suspend fun completeReminder(reminder: ReminderDomain): AppResult<Unit> {
        calls++
        return completion.await()
    }

    override suspend fun removeReminder(id: String): AppResult<Unit> = AppResult.Success(Unit)
}
