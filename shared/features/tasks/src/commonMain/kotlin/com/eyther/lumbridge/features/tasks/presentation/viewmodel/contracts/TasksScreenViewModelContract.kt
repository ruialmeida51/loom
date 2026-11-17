package com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.viewmodel.LoomViewModel
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

abstract class TasksScreenViewModelContract : LoomViewModel() {

    abstract val state: StateFlow<TasksState>

    abstract fun onRefresh()

    abstract fun onAddSheetVisibilityChanged(value: Boolean)

    abstract fun onTitleChanged(value: String)

    abstract fun onDateChanged(value: LocalDate)

    abstract fun onTimeChanged(value: LocalTime)

    abstract fun onRecurrenceChanged(value: TaskRecurrenceDomain)

    abstract fun onDueDateEnabledChanged(value: Boolean)

    abstract fun onNotificationEnabledChanged(value: Boolean)

    abstract fun onCompletedVisibilityChanged(value: Boolean)

    abstract fun onDismissError()

    abstract fun onSaveTask()

    abstract fun onCompleteTask(item: TaskDomain)

    abstract fun onRemoveTask(id: String)
}
