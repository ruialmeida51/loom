package com.eyther.lumbridge.features.reminders.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.observeWith
import com.eyther.lumbridge.core.coroutines.stateflow.updateState
import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.AddReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.CompleteReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.RemoveReminderUseCase
import com.eyther.lumbridge.features.reminders.presentation.mapper.toRemindersTextResource
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RemindersScreenViewModel(
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

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState = MutableStateFlow(RemindersState())
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
            getRemindersStreamUseCase().updateState(state) { result ->
                when (result) {
                    is AppResult.Success -> copy(items = result.value, error = null)
                    is AppResult.Failure -> copy(error = result.error.toRemindersTextResource())
                }
            }
        }

    override val state = mutableState.asStateFlow()

    override fun onAddSheetVisibilityChanged(value: Boolean) {
        if (value) inputState.value = InnerInputState()

        mutableState.update { oldState ->
            oldState.copy(showAdd = value, error = null)
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
            oldState.copy(error = null)
        }
    }

    override fun onSaveReminder() {
        if (state.value.busy) return
        val input = inputState.value

        mutableState.update { oldState ->
            oldState.copy(busy = true, error = null)
        }

        launch {
            when (val result = addReminderUseCase(
                title = input.title,
                date = input.date,
                time = input.time,
                recurrence = input.recurrence,
            )) {
                is AppResult.Success -> mutableState.update { oldState ->
                    oldState.copy(
                        items = oldState.items.filterNot { it.id == result.value.id } + result.value,
                        showAdd = false,
                        busy = false,
                    )
                }

                is AppResult.Failure -> mutableState.update { oldState ->
                    oldState.copy(
                        busy = false,
                        error = result.error.toRemindersTextResource(),
                    )
                }
            }
        }
    }

    override fun onCompleteReminder(item: ReminderDomain) {
        launch {
            val result = completeReminderUseCase(item)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        items = oldState.items.map { reminder ->
                            if (reminder.id == item.id && reminder.recurrence == "none") {
                                reminder.copy(completed = !reminder.completed)
                            } else {
                                reminder
                            }
                        },
                    )

                    is AppResult.Failure -> oldState.copy(
                        error = result.error.toRemindersTextResource(),
                    )
                }
            }
        }
    }

    override fun onRemoveReminder(id: String) {
        launch {
            val result = removeReminderUseCase(id)

            mutableState.update { oldState ->
                when (result) {
                    is AppResult.Success -> oldState.copy(
                        items = oldState.items.filterNot { it.id == id },
                    )

                    is AppResult.Failure -> oldState.copy(
                        error = result.error.toRemindersTextResource(),
                    )
                }
            }
        }
    }
}
