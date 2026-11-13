package com.eyther.lumbridge.features.reminders.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.*
import com.eyther.lumbridge.core.presentation.platformCollectAsStateOnStart
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.generated.resources.*
import com.eyther.lumbridge.features.reminders.presentation.localization.localizedMessage
import com.eyther.lumbridge.features.reminders.presentation.localization.localizedToken
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun RemindersScreen(vm: RemindersScreenViewModelContract, timeZone: String) {
    val state by vm.state.platformCollectAsStateOnStart()
    InnerRemindersScreen(
        items = state.items,
        title = state.title,
        date = state.date,
        time = state.time,
        recurrence = state.recurrence,
        showAdd = state.showAdd,
        busy = state.busy,
        error = state.error,
        timeZone = timeZone,
        onShowAdd = vm::showAdd,
        onTitle = vm::title,
        onDate = vm::date,
        onTime = vm::time,
        onRecurrence = vm::recurrence,
        onDismiss = vm::dismiss,
        onAdd = vm::add,
        onComplete = vm::complete,
        onRemove = vm::remove,
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
    error: String?,
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
        item { ErrorNotice(localizedMessage(error), onDismiss) }
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
            items.sortedWith(compareBy<ReminderDomain> { it.completed }.thenBy { it.dueAt }),
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
                                    localizedToken(reminder.recurrence),
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
            onDismissRequest = { if (!busy) onShowAdd(false) },
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
                                        localizedToken(repeat),
                                    )
                                },
                            )
                        }
                    }
                    ErrorNotice(localizedMessage(error), onDismiss)
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
