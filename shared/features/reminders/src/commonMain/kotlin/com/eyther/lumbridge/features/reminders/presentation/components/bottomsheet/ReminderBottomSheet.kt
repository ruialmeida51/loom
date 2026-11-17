package com.eyther.lumbridge.features.reminders.presentation.components.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.reminders.generated.resources.Res
import com.eyther.lumbridge.features.reminders.generated.resources.ui_a_little_reminder
import com.eyther.lumbridge.features.reminders.generated.resources.ui_cancel
import com.eyther.lumbridge.features.reminders.generated.resources.ui_date_yyyy_mm_dd
import com.eyther.lumbridge.features.reminders.generated.resources.ui_save_reminder
import com.eyther.lumbridge.features.reminders.generated.resources.ui_time_hh_mm
import com.eyther.lumbridge.features.reminders.generated.resources.ui_times_are_in_1_s
import com.eyther.lumbridge.features.reminders.generated.resources.ui_what_needs_doing
import com.eyther.lumbridge.features.reminders.presentation.mapper.reminderRecurrenceLabel
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReminderBottomSheet(
    title: String,
    date: String,
    time: String,
    recurrence: String,
    busy: Boolean,
    formError: TextResource?,
    timeZone: String,
    onShowAdd: (Boolean) -> Unit,
    onTitle: (String) -> Unit,
    onDate: (String) -> Unit,
    onTime: (String) -> Unit,
    onRecurrence: (String) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_a_little_reminder),
        action = {
            LoomButton(stringResource(Res.string.ui_save_reminder), onAdd, loading = busy)
        },
        dismissAction = {
            OutlinedButton(onClick = { onShowAdd(false) }, enabled = !busy) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        OutlinedTextField(
            title,
            onTitle,
            label = { Text(stringResource(Res.string.ui_what_needs_doing)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        OutlinedTextField(
            date,
            onDate,
            label = { Text(stringResource(Res.string.ui_date_yyyy_mm_dd)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        OutlinedTextField(
            time,
            onTime,
            label = { Text(stringResource(Res.string.ui_time_hh_mm)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = !busy,
        )
        Text(
            stringResource(Res.string.ui_times_are_in_1_s, timeZone),
            style = MaterialTheme.typography.bodySmall,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("none", "daily", "weekly").forEach { repeat ->
                FilterChip(
                    recurrence == repeat,
                    { onRecurrence(repeat) },
                    label = {
                        Text(
                            reminderRecurrenceLabel(repeat),
                        )
                    },
                    enabled = !busy,
                )
            }
        }
        formError?.let { message ->
            ErrorNotice(message = message, onDismiss = onDismiss)
        }
    }
}
