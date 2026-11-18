package com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.presentation.model.DestructiveConfirmationUi
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.task_delete_confirm
import com.eyther.lumbridge.features.tasks.generated.resources.task_delete_message
import com.eyther.lumbridge.features.tasks.generated.resources.task_delete_recurring_message
import com.eyther.lumbridge.features.tasks.generated.resources.task_delete_title
import com.eyther.lumbridge.features.tasks.generated.resources.ui_cancel
import com.eyther.lumbridge.features.tasks.presentation.mapper.toDateUi
import com.eyther.lumbridge.features.tasks.presentation.model.TaskRowUi
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class TasksState(
    val items: List<TaskDomain> = emptyList(),
    val timeZone: String = "UTC",
    val today: String = todayIso(),
    val refreshing: Boolean = false,
    val notificationPrimeRequest: Int = 0,
    val completingIds: PersistentSet<String> = persistentSetOf(),
    val removingIds: PersistentSet<String> = persistentSetOf(),
    val deletingTask: TaskDomain? = null,
    val deleteError: TextResource? = null,
    val successMessage: TextResource? = null,
    val loading: Boolean = true,
    val title: String = "",
    val date: LocalDate = LocalDate.parse(todayIso()),
    val time: LocalTime = LocalTime(hour = 18, minute = 0),
    val recurrence: TaskRecurrenceDomain = TaskRecurrenceDomain.NONE,
    val hasDueDate: Boolean = false,
    val notificationEnabled: Boolean = false,
    val showCompleted: Boolean = false,
    val showAdd: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
    val formError: TextResource? = null,
) {

    val deleteConfirmation: DestructiveConfirmationUi? = deletingTask?.let { task ->
        DestructiveConfirmationUi(
            title = TextResource.Resource(Res.string.task_delete_title),
            message =
                TextResource.Resource(
                    if (task.recurrence == TaskRecurrenceDomain.NONE) Res.string.task_delete_message
                    else Res.string.task_delete_recurring_message,
                    args = listOf(task.title),
                ),
            confirmText = TextResource.Resource(Res.string.task_delete_confirm),
            cancelText = TextResource.Resource(Res.string.ui_cancel),
        )
    }
    val deleting: Boolean = deletingTask?.id in removingIds

    val displayError: TextResource? = error ?: loadError
    val showEmpty: Boolean =
        !loading && !refreshing && items.none { !it.completed } && displayError == null

    val orderedTasks: List<TaskDomain> =
        items.sortedWith(
            compareBy<TaskDomain> { it.completed }
                .thenBy { it.dueAt == null }
                .thenBy { it.dueAt }
                .thenBy { it.id },
        )

    val rows =
        orderedTasks
            .map {
                TaskRowUi(
                    task = it,
                    date = it.toDateUi(today = LocalDate.parse(today), timeZone = timeZone),
                )
            }
            .toImmutableList()

    val openRows = rows.filterNot { it.task.completed }.toImmutableList()
    val completedRows = rows.filter { it.task.completed }.toImmutableList()
}
