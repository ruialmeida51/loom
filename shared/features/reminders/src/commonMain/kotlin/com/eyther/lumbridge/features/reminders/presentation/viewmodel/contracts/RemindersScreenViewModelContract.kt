package com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.eyther.lumbridge.core.domain.*
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import kotlinx.coroutines.flow.*

data class RemindersState(
    val items: List<ReminderDomain> = emptyList(),
    val title: String = "",
    val date: String = todayIso(),
    val time: String = "18:00",
    val recurrence: String = "none",
    val showAdd: Boolean = false,
    val busy: Boolean = false,
    val error: String? = null,
)

abstract class RemindersScreenViewModelContract : LoomViewModel() {
    abstract val state: StateFlow<RemindersState>

    abstract fun showAdd(value: Boolean)

    abstract fun title(value: String)

    abstract fun date(value: String)

    abstract fun time(value: String)

    abstract fun recurrence(value: String)

    abstract fun dismiss()

    abstract fun add()

    abstract fun complete(item: ReminderDomain)

    abstract fun remove(id: String)
}
