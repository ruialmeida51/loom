package com.eyther.lumbridge.features.reminders.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.presentation.components.snackbar.DEFAULT_ALERT_DURATION
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.AddReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.CompleteReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.RemoveReminderUseCase
import com.eyther.lumbridge.features.reminders.generated.resources.Res
import com.eyther.lumbridge.features.reminders.generated.resources.reminder_completed
import com.eyther.lumbridge.features.reminders.generated.resources.reminder_removed
import com.eyther.lumbridge.features.reminders.generated.resources.reminder_reopened
import com.eyther.lumbridge.features.reminders.presentation.mapper.toRemindersTextResource
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class RemindersScreenViewModel(
    private val context: HouseholdContextDomain,
    private val getRemindersStreamUseCase: GetRemindersStreamUseCase,
    private val addReminderUseCase: AddReminderUseCase,
    private val completeReminderUseCase: CompleteReminderUseCase,
    private val removeReminderUseCase: RemoveReminderUseCase,
) : RemindersScreenViewModelContract() {

    private data class InnerInputState(
        val title: String = "",
        val date: String = todayIso(),
        val time: String = "18:00",
        val recurrence: String = "none",
    )

    private val refreshRequests = MutableStateFlow(0)
    private var feedbackJob: Job? = null

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(
                RemindersState(timeZone = context.timeZone, today = todayIso(context.timeZone)),
            )
            .observeWith(viewModelScope) { state ->
                inputState.updateState(state) { input ->
                    copy(
                        title = input.title,
                        date = input.date,
                        time = input.time,
                        recurrence = input.recurrence,
                    )
                }
            }
            .observeWith(viewModelScope) { state ->
                refreshRequests
                    .flatMapLatest { getRemindersStreamUseCase() }
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
                                    loadError = result.error.toRemindersTextResource(),
                                )
                        }
                    }
            }

    override val state = mutableState.asStateFlow()

    override fun onAddSheetVisibilityChanged(value: Boolean) {
        if (!value && state.value.busy) return

        if (value) inputState.value = InnerInputState()

        mutableState.update { oldState ->
            oldState.copy(showAdd = value, formError = null)
        }
    }

    override fun onTitleChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(title = value)
        }
    }

    override fun onDateChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(date = value)
        }
    }

    override fun onTimeChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(time = value)
        }
    }

    override fun onRecurrenceChanged(value: String) {
        inputState.update { oldState ->
            oldState.copy(recurrence = value)
        }
    }

    override fun onDismissError() {
        mutableState.update { oldState ->
            oldState.copy(error = null, loadError = null, formError = null)
        }
    }

    override fun onSaveReminder() {
        if (state.value.busy) return
        val input = inputState.value

        mutableState.update { oldState ->
            oldState.copy(busy = true, formError = null)
        }

        launch {
            when (
                val result =
                    addReminderUseCase(
                        title = input.title,
                        date = input.date,
                        time = input.time,
                        recurrence = input.recurrence,
                    )
            ) {
                is AppResult.Success ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            items =
                                oldState.items.filterNot { it.id == result.value.id } +
                                    result.value,
                            showAdd = false,
                            busy = false,
                        )
                    }

                is AppResult.Failure ->
                    mutableState.update { oldState ->
                        oldState.copy(
                            busy = false,
                            formError = result.error.toRemindersTextResource(),
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

    override fun onCompleteReminder(item: ReminderDomain) {
        if (item.id in state.value.completingIds || item.id in state.value.removingIds) return
        mutableState.update { oldState ->
            oldState.copy(completingIds = oldState.completingIds.add(item.id), error = null)
        }
        launch {
            val result = completeReminderUseCase(reminder = item)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            completingIds = oldState.completingIds.remove(item.id),
                            items =
                                oldState.items.map { reminder ->
                                    if (reminder.id == item.id && reminder.recurrence == "none")
                                        reminder.copy(completed = !item.completed)
                                    else reminder
                                },
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            completingIds = oldState.completingIds.remove(item.id),
                            error = result.error.toRemindersTextResource(),
                        )
                }
            }
            if (result is AppResult.Success) {
                showSuccess(
                    TextResource.Resource(
                        if (item.completed) Res.string.reminder_reopened
                        else Res.string.reminder_completed,
                        args = listOf(item.title),
                    ),
                )
                refreshRequests.update { oldState -> oldState + 1 }
            }
        }
    }

    override fun onRemoveReminder(id: String) {
        if (id in state.value.completingIds || id in state.value.removingIds) return
        mutableState.update { oldState ->
            oldState.copy(removingIds = oldState.removingIds.add(id), error = null)
        }
        launch {
            val result = removeReminderUseCase(id)
            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success ->
                        oldState.copy(
                            items = oldState.items.filterNot { it.id == id },
                            removingIds = oldState.removingIds.remove(id),
                        )
                    is AppResult.Failure ->
                        oldState.copy(
                            removingIds = oldState.removingIds.remove(id),
                            error = result.error.toRemindersTextResource(),
                        )
                }
            }
            if (result is AppResult.Success)
                showSuccess(TextResource.Resource(Res.string.reminder_removed))
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
