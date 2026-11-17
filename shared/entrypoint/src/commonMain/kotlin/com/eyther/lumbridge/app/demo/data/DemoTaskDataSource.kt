package com.eyther.lumbridge.app.demo.data

import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.tasks.domain.datasource.TaskDataSource
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class DemoTaskDataSource(private val store: DemoHouseholdStore) : TaskDataSource {

    override fun stream() = store.tasks.map { AppResult.Success(it) }

    override suspend fun addTask(task: TaskDomain) = safeCall {
        store.tasks.update { it + task }
        Unit
    }

    override suspend fun completeTask(task: TaskDomain) = safeCall {
        store.tasks.update {
            it.map { row ->
                if (row.id != task.id || row.dueAt != task.dueAt) row
                else if (row.recurrence != TaskRecurrenceDomain.NONE && !row.completed)
                    row.copy(dueAt = row.nextDue(store.context.timeZone))
                else row.copy(completed = !row.completed)
            }
        }
        Unit
    }

    override suspend fun removeTask(id: String) = safeCall {
        store.tasks.update { it.filterNot { row -> row.id == id } }
        Unit
    }
}
