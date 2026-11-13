package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.safeCall
import com.eyther.lumbridge.features.reminders.domain.datasource.ReminderDataSource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.coroutines.flow.update

class DemoReminderDataSource(private val store: DemoHouseholdStore) : ReminderDataSource {
    override fun observe() = store.reminders

    override suspend fun add(reminder: ReminderDomain) = safeCall {
        store.reminders.update { it + reminder }
        Unit
    }

    override suspend fun complete(reminder: ReminderDomain) = safeCall {
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

    override suspend fun remove(id: String) = safeCall {
        store.reminders.update { it.filterNot { row -> row.id == id } }
        Unit
    }
}
