package com.eyther.lumbridge.features.reminders.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.EmptyNotice
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.LoomCard
import com.eyther.lumbridge.core.presentation.components.PageHeading
import com.eyther.lumbridge.core.presentation.extensions.platformCollectAsStateOnStart
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.generated.resources.Res
import com.eyther.lumbridge.features.reminders.generated.resources.ui_a_gentle_nudge
import com.eyther.lumbridge.features.reminders.generated.resources.ui_a_little_reminder
import com.eyther.lumbridge.features.reminders.generated.resources.ui_add_a_reminder
import com.eyther.lumbridge.features.reminders.generated.resources.ui_add_a_reminder_and_let_loom_keep_track_of
import com.eyther.lumbridge.features.reminders.generated.resources.ui_cancel
import com.eyther.lumbridge.features.reminders.generated.resources.ui_date_yyyy_mm_dd
import com.eyther.lumbridge.features.reminders.generated.resources.ui_nothing_on_your_mind
import com.eyther.lumbridge.features.reminders.generated.resources.ui_remove
import com.eyther.lumbridge.features.reminders.generated.resources.ui_repeats_1_s
import com.eyther.lumbridge.features.reminders.generated.resources.ui_room_in_your_head
import com.eyther.lumbridge.features.reminders.generated.resources.ui_save_reminder
import com.eyther.lumbridge.features.reminders.generated.resources.ui_the_little_things_remembered_together
import com.eyther.lumbridge.features.reminders.generated.resources.ui_time_hh_mm
import com.eyther.lumbridge.features.reminders.generated.resources.ui_times_are_in_1_s
import com.eyther.lumbridge.features.reminders.generated.resources.ui_what_needs_doing
import com.eyther.lumbridge.features.reminders.presentation.mapper.reminderRecurrenceLabel
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import org.jetbrains.compose.resources.stringResource

@Composable
fun RemindersScreen(viewModel: RemindersScreenViewModelContract, timeZone: String) {
    val state by viewModel.state.platformCollectAsStateOnStart()
    InnerRemindersScreen(
        items = state.orderedReminders,
        title = state.title,
        date = state.date,
        time = state.time,
        recurrence = state.recurrence,
        showAdd = state.showAdd,
        busy = state.busy,
        error = state.displayError,
        formError = state.formError,
        timeZone = timeZone,
        onShowAdd = viewModel::onAddSheetVisibilityChanged,
        onTitle = viewModel::onTitleChanged,
        onDate = viewModel::onDateChanged,
        onTime = viewModel::onTimeChanged,
        onRecurrence = viewModel::onRecurrenceChanged,
        onDismiss = viewModel::onDismissError,
        onAdd = viewModel::onSaveReminder,
        onComplete = viewModel::onCompleteReminder,
        onRemove = viewModel::onRemoveReminder,
    )
}

@Composable
private fun InnerRemindersScreen(
    items: List<ReminderDomain>,
    title: String,
    date: String,
    time: String,
    recurrence: String,
    showAdd: Boolean,
    busy: Boolean,
    error: TextResource?,
    formError: TextResource?,
    timeZone: String,
    onShowAdd: (Boolean) -> Unit,
    onTitle: (String) -> Unit,
    onDate: (String) -> Unit,
    onTime: (String) -> Unit,
    onRecurrence: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    onComplete: (ReminderDomain) -> Unit,
    onRemove: (String) -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            PageHeading(
                stringResource(Res.string.ui_a_gentle_nudge),
                stringResource(Res.string.ui_room_in_your_head),
                stringResource(Res.string.ui_the_little_things_remembered_together),
            )
        }
        error?.let { message ->
            item(key = "error") {
                ErrorNotice(message = message, onDismiss = onDismiss)
            }
        }
        item {
            LoomButton(
                stringResource(Res.string.ui_add_a_reminder),
                { onShowAdd(true) },
                Modifier.fillMaxWidth(),
            )
        }
        if (items.isEmpty())
            item {
                EmptyNotice(
                    stringResource(Res.string.ui_nothing_on_your_mind),
                    stringResource(Res.string.ui_add_a_reminder_and_let_loom_keep_track_of),
                )
            }
        items(
            items,
            key = { it.id },
        ) { reminder ->
            LoomCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(reminder.completed, { onComplete(reminder) })
                    Column(Modifier.weight(1f)) {
                        Text(reminder.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            reminder.dueLabel(timeZone),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (reminder.recurrence != "none")
                            Text(
                                stringResource(
                                    Res.string.ui_repeats_1_s,
                                    reminderRecurrenceLabel(reminder.recurrence),
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                    }
                    TextButton(onClick = { onRemove(reminder.id) }) {
                        Text(
                            stringResource(Res.string.ui_remove),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
    if (showAdd)
        AlertDialog(
            onDismissRequest = { onShowAdd(false) },
            title = { Text(stringResource(Res.string.ui_a_little_reminder)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        title,
                        onTitle,
                        label = { Text(stringResource(Res.string.ui_what_needs_doing)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        date,
                        onDate,
                        label = { Text(stringResource(Res.string.ui_date_yyyy_mm_dd)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        time,
                        onTime,
                        label = { Text(stringResource(Res.string.ui_time_hh_mm)) },
                        singleLine = true,
                    )
                    Text(
                        stringResource(Res.string.ui_times_are_in_1_s, timeZone),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("none", "daily", "weekly").forEach { repeat ->
                            FilterChip(
                                recurrence == repeat,
                                { onRecurrence(repeat) },
                                label = {
                                    Text(
                                        reminderRecurrenceLabel(repeat),
                                    )
                                },
                            )
                        }
                    }
                    formError?.let { message ->
                        ErrorNotice(message = message, onDismiss = onDismiss)
                    }
                }
            },
            confirmButton = {
                LoomButton(stringResource(Res.string.ui_save_reminder), onAdd, loading = busy)
            },
            dismissButton = {
                TextButton(onClick = { onShowAdd(false) }, enabled = !busy) {
                    Text(stringResource(Res.string.ui_cancel))
                }
            },
        )
}
