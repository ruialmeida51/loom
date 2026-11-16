package com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.presentation.mapper.toDateUi
import com.eyther.lumbridge.features.reminders.presentation.model.ReminderRowUi
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDate

data class RemindersState(
    val items: List<ReminderDomain> = emptyList(),
    val timeZone: String = "UTC",
    val today: String = todayIso(),
    val refreshing: Boolean = false,
    val completingIds: PersistentSet<String> = persistentSetOf(),
    val removingIds: PersistentSet<String> = persistentSetOf(),
    val successMessage: TextResource? = null,
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
    val showEmpty: Boolean = !loading && !refreshing && items.isEmpty() && displayError == null

    val orderedReminders: List<ReminderDomain> =
        items.sortedWith(
            compareBy<ReminderDomain> { it.completed }.thenBy { it.dueAt },
        )

    val rows =
        orderedReminders
            .map {
                ReminderRowUi(
                    reminder = it,
                    date = it.toDateUi(today = LocalDate.parse(today), timeZone = timeZone),
                )
            }
            .toImmutableList()
}
