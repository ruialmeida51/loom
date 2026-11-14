package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DemoReminderDataSource(private val store: DemoHouseholdStore) : ReminderDataSource {

    override fun stream() = store.reminders.map { AppResult.Success(it) }

    override suspend fun addReminder(reminder: ReminderDomain) = safeCall {
        store.reminders.update { it + reminder }
        Unit
    }

    override suspend fun completeReminder(reminder: ReminderDomain) = safeCall {
        store.reminders.update {
            it.map { row ->
                if (row.id != reminder.id || row.dueAt != reminder.dueAt) row
                else if (row.recurrence != "none" && !row.completed)
                    row.copy(dueAt = row.nextDue(store.context.timeZone))
                else row.copy(completed = !row.completed)
            }
        }
        Unit
    }

    override suspend fun removeReminder(id: String) = safeCall {
        store.reminders.update { it.filterNot { row -> row.id == id } }
        Unit
    }
}
