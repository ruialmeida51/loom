package com.eyther.lumbridge.features.reminders.presentation.viewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import com.eyther.lumbridge.core.coroutines.stateflow.*
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.AddReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.CompleteReminderUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.ObserveRemindersUseCase
import com.eyther.lumbridge.features.reminders.domain.usecase.RemoveReminderUseCase
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersState
import kotlinx.coroutines.flow.*

class RemindersScreenViewModel(
    observe: ObserveRemindersUseCase,
    private val addReminder: AddReminderUseCase,
    private val completeReminder: CompleteReminderUseCase,
    private val removeReminder: RemoveReminderUseCase,
) : RemindersScreenViewModelContract() {
    private data class InnerInputState(
        val title: String = "",
        val date: String = todayIso(),
        val time: String = "18:00",
        val recurrence: String = "none",
    )

    private val inputState = MutableStateFlow(InnerInputState())

    private val mutableState =
        MutableStateFlow(RemindersState()).observeWith(viewModelScope) { state ->
            inputState.updateState(state) {
                copy(title = it.title, date = it.date, time = it.time, recurrence = it.recurrence)
            }
        }
    override val state = mutableState.asStateFlow()

    init {
        mutableState.observeWith(viewModelScope) {
            observe()
                .catch { error -> mutableState.update { it.copy(error = error.message) } }
                .onEach { items -> mutableState.update { it.copy(items = items) } }
        }
    }

    override fun showAdd(value: Boolean) {
        if (value) inputState.value = InnerInputState()
        mutableState.update { it.copy(showAdd = value) }
    }

    override fun title(value: String) {
        inputState.update { it.copy(title = value) }
    }

    override fun date(value: String) {
        inputState.update { it.copy(date = value) }
    }

    override fun time(value: String) {
        inputState.update { it.copy(time = value) }
    }

    override fun recurrence(value: String) {
        inputState.update { it.copy(recurrence = value) }
    }

    override fun dismiss() {
        mutableState.update { it.copy(error = null) }
    }

    override fun add() {
        if (mutableState.value.busy) return
        val input = inputState.value
        mutableState.update { it.copy(busy = true, error = null) }
        launch {
            when (val result = addReminder(input.title, input.date, input.time, input.recurrence)) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        old.copy(
                            items = old.items.filterNot { it.id == result.value.id } + result.value,
                            title = "",
                            showAdd = false,
                            busy = false,
                        )
                    }
                is AppResult.Failure ->
                    mutableState.update { it.copy(busy = false, error = result.message) }
            }
        }
    }

    override fun complete(item: ReminderDomain) {
        launch {
            when (val result = completeReminder(item)) {
                is AppResult.Success -> {
                    if (item.recurrence == "none")
                        mutableState.update { old ->
                            old.copy(
                                items =
                                    old.items.map {
                                        if (it.id == item.id) it.copy(completed = !item.completed)
                                        else it
                                    },
                            )
                        }
                }
                is AppResult.Failure -> mutableState.update { it.copy(error = result.message) }
            }
        }
    }

    override fun remove(id: String) {
        launch {
            when (val result = removeReminder(id)) {
                is AppResult.Success ->
                    mutableState.update { old ->
                        old.copy(items = old.items.filterNot { it.id == id })
                    }
                is AppResult.Failure -> mutableState.update { it.copy(error = result.message) }
            }
        }
    }
}
