package com.eyther.lumbridge.features.tasks.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.extensions.delayFirstAtLeast
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.components.snackbar.DEFAULT_ALERT_DURATION
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.household.domain.usecase.GetMembersStreamUseCase
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.domain.usecase.AddTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.AssignTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.CompleteTaskUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.GetTasksStreamUseCase
import com.eyther.lumbridge.features.tasks.domain.usecase.RemoveTaskUseCase
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.task_added
import com.eyther.lumbridge.features.tasks.generated.resources.task_completed
import com.eyther.lumbridge.features.tasks.generated.resources.task_removed
import com.eyther.lumbridge.features.tasks.generated.resources.task_reopened
import com.eyther.lumbridge.features.tasks.presentation.mapper.toTasksTextResource
import com.eyther.lumbridge.features.tasks.presentation.model.TaskFilterUi
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts.TasksScreenViewModelContract
import com.eyther.lumbridge.features.tasks.presentation.viewmodel.contracts.TasksState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class TasksScreenViewModel(
    private val context: HouseholdContextDomain,
    private val getTasksStreamUseCase: GetTasksStreamUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val removeTaskUseCase: RemoveTaskUseCase,
    private val assignTaskUseCase: AssignTaskUseCase,
    private val getMembersStreamUseCase: GetMembersStreamUseCase,
) : TasksScreenViewModelContract() {

    private data class InnerInputState(
        val title: String = "",
        val date: LocalDate = LocalDate.parse(todayIso()),
        val time: LocalTime = LocalTime(hour = 18, minute = 0),
        val recurrence: TaskRecurrenceDomain = TaskRecurrenceDomain.NONE,
        val hasDueDate: Boolean = false,
        val notificationEnabled: Boolean = false,
        val assigneeId: String? = null,
        val takesTurns: Boolean = false,
    )

    private val refreshRequests = MutableStateFlow(0)
    private var feedbackJob: Job? = null

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(
                TasksState(
                    timeZone = context.timeZone,
                    today = todayIso(context.timeZone),
                    userId = context.userId,
                ),
            )
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(
                        title = input.title,
                        date = input.date,
                        time = input.time,
                        recurrence = input.recurrence,
                        hasDueDate = input.hasDueDate,
                        notificationEnabled = input.notificationEnabled,
                        assigneeId = input.assigneeId,
                        takesTurns = input.takesTurns,
                    )
                }
            }
            .observeWith(viewModelScope) { state ->
                // Names are a nicety; a failure leaves tasks usable without them.
                refreshRequests
                    .flatMapLatest { getMembersStreamUseCase(householdId = context.id) }
                    .updateState(state) { result ->
                        when (result) {
                            is AppResult.Success -> copy(members = result.value)
                            is AppResult.Failure -> this
                        }
                    }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest {
                        getTasksStreamUseCase()
                            .delayFirstAtLeast(
                                enabled = state.value.loading || state.value.refreshing,
                            )
                    }
                    .map { it to todayIso(context.timeZone) }
                    .updateState(state) { (result, currentDate) ->
                        when (result) {
                            is AppResult.Success ->
                                copy(
                                    items = result.value,
                                    loading = false,
                                    refreshing = false,
                                    today = currentDate,
                                    loadError = null,
                                )
                            is AppResult.Failure ->
                                copy(
                                    loading = false,
                                    refreshing = false,
                                    today = currentDate,
                                    loadError = result.error.toTasksTextResource(),
                                )
                        }
                    }
            }

    override val state = mutableState.asStateFlow()

    override fun onAddSheetVisibilityChanged(value: Boolean) {
        if (!value && state.value.busy) return

        if (value)
            inputState.value = InnerInputState(date = LocalDate.parse(todayIso(context.timeZone)))

        mutableState.update { oldState ->
            oldState.copy(showAdd = value, formError = null)
        }
    }

    override fun onTitleChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(title = value)
        }
    }

    override fun onDateChanged(value: LocalDate) {
        inputState.update { oldState ->
            oldState.copy(date = value, hasDueDate = true)
        }
    }

    override fun onTimeChanged(value: LocalTime) {
        inputState.update { oldState ->
            oldState.copy(time = value)
        }
    }

    override fun onRecurrenceChanged(value: TaskRecurrenceDomain) {
        inputState.update { oldState ->
            oldState.copy(recurrence = value)
        }
    }

    override fun onDueDateEnabledChanged(value: Boolean) {
        inputState.update { oldState ->
            oldState.copy(
                hasDueDate = value,
                notificationEnabled = value && oldState.notificationEnabled,
                recurrence = if (value) oldState.recurrence else TaskRecurrenceDomain.NONE,
            )
        }
    }

    override fun onNotificationEnabledChanged(value: Boolean) {
        inputState.update { oldState -> oldState.copy(notificationEnabled = value) }
    }

    override fun onCompletedVisibilityChanged(value: Boolean) {
        mutableState.update { oldState -> oldState.copy(showCompleted = value) }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null, formError = null)
        }
    }

    override fun onAssigneeChanged(id: String?) {
        inputState.update { oldState ->
            oldState.copy(assigneeId = id, takesTurns = id != null && oldState.takesTurns)
        }
    }

    override fun onTakesTurnsChanged(value: Boolean) {
        inputState.update { oldState -> oldState.copy(takesTurns = value) }
    }

    override fun onFilterChanged(value: TaskFilterUi) {
        mutableState.update { oldState -> oldState.copy(filter = value) }
    }

    override fun onAssignRequested(task: TaskDomain) {
        mutableState.update { oldState ->
            oldState.copy(
                assigningTask = task,
                assignDraftId = task.assigneeId,
                assignDraftTurns = task.takesTurns,
                error = null,
            )
        }
    }

    override fun onAssignDraftChanged(id: String?) {
        mutableState.update { oldState ->
            oldState.copy(
                assignDraftId = id,
                assignDraftTurns = id != null && oldState.assignDraftTurns,
            )
        }
    }

    override fun onAssignDraftTurnsChanged(value: Boolean) {
        mutableState.update { oldState -> oldState.copy(assignDraftTurns = value) }
    }

    override fun onAssignDismissed() {
        if (state.value.assigning) return
        mutableState.update { oldState -> oldState.copy(assigningTask = null) }
    }

    override fun onAssignConfirmed() {
        val current = state.value
        val task = current.assigningTask ?: return
        if (current.assigning) return
        mutableState.update { oldState -> oldState.copy(assigning = true) }
        launch {
            val result =
                assignTaskUseCase(
                    task = task,
                    assigneeId = current.assignDraftId,
                    takesTurns = current.assignDraftTurns && current.assignCanTakeTurns,
                    memberIds = current.members.map { it.id },
                )
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            assigning = false,
                            assigningTask = null,
                            items =
                                oldState.items.map {
                                    if (it.id == result.value.id) result.value else it
                                },
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            assigning = false,
                            assigningTask = null,
                            error = result.error.toTasksTextResource(),
                        )
                }
            }
        }
    }

    override fun onSaveTask() {
        if (state.value.busy) return
        val input = inputState.value

        mutableState.update { oldState ->
            oldState.copy(busy = true, formError = null)
        }

        launch {
            when (
                val result =
                    addTaskUseCase(
                        title = input.title,
                        date = input.date.takeIf { input.hasDueDate },
                        time = input.time,
                        recurrence = input.recurrence,
                        notificationEnabled = input.notificationEnabled,
                        assigneeId = input.assigneeId,
                        takesTurns = input.takesTurns && state.value.canTakeTurns,
                        memberIds = state.value.members.map { it.id },
                    )
            ) {
                is AppResult.Success -> {
                    mutableState.update { oldState ->
                        oldState.copy(
                            items =
                                oldState.items.filterNot { it.id == result.value.id } +
                                    result.value,
                            showAdd = false,
                            notificationPrimeRequest =
                                oldState.notificationPrimeRequest +
                                    if (input.hasDueDate && input.notificationEnabled) 1 else 0,
                            busy = false,
                        )
                    }
                    showSuccess(TextResource.Resource(Res.string.task_added))
                }

                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = false,
                            formError = result.error.toTasksTextResource(),
                        )
                    }
            }
        }
    }

    override fun onRefresh() {
        if (state.value.refreshing) return
        mutableState.update { oldState -> oldState.copy(refreshing = true, loadError = null) }
        refreshRequests.update { oldState -> oldState + 1 }
    }

    override fun onCompleteTask(item: TaskDomain) {
        if (item.id in state.value.completingIds || item.id in state.value.removingIds) return
        mutableState.update { oldState ->
            oldState.copy(completingIds = oldState.completingIds.add(item.id), error = null)
        }
        launch {
            val result = completeTaskUseCase(task = item)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            completingIds = oldState.completingIds.remove(item.id),
                            items =
                                oldState.items.map { task ->
                                    if (
                                        task.id == item.id &&
                                            task.recurrence == TaskRecurrenceDomain.NONE
                                    )
                                        task.copy(completed = !item.completed)
                                    else task
                                },
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            completingIds = oldState.completingIds.remove(item.id),
                            error = result.error.toTasksTextResource(),
                        )
                }
            }
            if (result is AppResult.Success) {
                showSuccess(
                    TextResource.Resource(
                        if (item.completed) Res.string.task_reopened else Res.string.task_completed,
                        args = listOf(item.title),
                    ),
                )
                refreshRequests.update { oldState -> oldState + 1 }
            }
        }
    }

    override fun onRemoveTask(id: String) {
        if (id in state.value.completingIds || id in state.value.removingIds) return
        val task = state.value.items.firstOrNull { it.id == id } ?: return
        mutableState.update { oldState -> oldState.copy(deletingTask = task, deleteError = null) }
    }

    override fun onRemoveTaskCancelled() {
        if (state.value.deleting) return
        mutableState.update { oldState -> oldState.copy(deletingTask = null, deleteError = null) }
    }

    override fun onRemoveTaskConfirmed() {
        val id = state.value.deletingTask?.id ?: return
        if (id in state.value.completingIds || id in state.value.removingIds) return
        mutableState.update { oldState ->
            oldState.copy(removingIds = oldState.removingIds.add(id), deleteError = null)
        }
        launch {
            val result = removeTaskUseCase(id)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            items = oldState.items.filterNot { it.id == id },
                            removingIds = oldState.removingIds.remove(id),
                            deletingTask = null,
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            removingIds = oldState.removingIds.remove(id),
                            deleteError = result.error.toTasksTextResource(),
                        )
                }
            }
            if (result is AppResult.Success)
                showSuccess(TextResource.Resource(Res.string.task_removed))
        }
    }

    private fun showSuccess(message: TextResource) {
        feedbackJob?.cancel()
        mutableState.update { oldState -> oldState.copy(successMessage = message) }
        feedbackJob = launch {
            delay(DEFAULT_ALERT_DURATION)
            mutableState.update { oldState -> oldState.copy(successMessage = null) }
        }
    }
}
