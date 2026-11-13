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
import com.eyther.lumbridge.features.reminders.presentation.viewmodel.contracts.RemindersScreenViewModelContract
import kotlinx.coroutines.flow.*

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
                "A GENTLE NUDGE",
                "Room in your head.",
                "The little things, remembered together.",
            )
        }
        item { ErrorNotice(error, onDismiss) }
        item { LoomButton("Add a reminder", { onShowAdd(true) }, Modifier.fillMaxWidth()) }
        if (items.isEmpty())
            item {
                EmptyNotice(
                    "Nothing on your mind.",
                    "Add a reminder and let Loom keep track of it.",
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
                                "Repeats ${reminder.recurrence}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                    }
                    TextButton(onClick = { onRemove(reminder.id) }) {
                        Text("Remove", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
    if (showAdd)
        AlertDialog(
            onDismissRequest = { if (!busy) onShowAdd(false) },
            title = { Text("A little reminder") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        title,
                        onTitle,
                        label = { Text("What needs doing?") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        date,
                        onDate,
                        label = { Text("Date · YYYY-MM-DD") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        time,
                        onTime,
                        label = { Text("Time · HH:mm") },
                        singleLine = true,
                    )
                    Text("Times are in $timeZone", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        listOf("none", "daily", "weekly").forEach { repeat ->
                            FilterChip(
                                recurrence == repeat,
                                { onRecurrence(repeat) },
                                label = {
                                    Text(
                                        if (repeat == "none") "Once"
                                        else repeat.replaceFirstChar { it.uppercase() },
                                    )
                                },
                            )
                        }
                    }
                    ErrorNotice(error, onDismiss)
                }
            },
            confirmButton = { LoomButton("Save reminder", onAdd, loading = busy) },
            dismissButton = {
                TextButton(onClick = { onShowAdd(false) }, enabled = !busy) { Text("Cancel") }
            },
        )
}
