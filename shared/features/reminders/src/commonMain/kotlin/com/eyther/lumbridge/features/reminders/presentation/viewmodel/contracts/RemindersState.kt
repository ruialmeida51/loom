package com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain

data class RemindersState(
    val items: List<ReminderDomain> = emptyList(),
    val loading: Boolean = true,
    val title: String = "",
    val date: String = todayIso(),
    val time: String = "18:00",
    val recurrence: String = "none",
    val showAdd: Boolean = false,
    val busy: Boolean = false,
    val error: TextResource? = null,
    val loadError: TextResource? = null,
    val formError: TextResource? = null,
) {

    val displayError: TextResource? = error ?: loadError
    val showEmpty: Boolean = !loading && items.isEmpty() && displayError == null

    val orderedReminders: List<ReminderDomain> =
        items.sortedWith(
            compareBy<ReminderDomain> { it.completed }.thenBy { it.dueAt },
        )
}
