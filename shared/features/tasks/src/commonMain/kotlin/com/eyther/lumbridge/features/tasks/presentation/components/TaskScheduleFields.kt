package com.eyther.lumbridge.features.tasks.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.eyther.lumbridge.core.presentation.components.DestructiveIconButton
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.resources.calendarDateText
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.task_add_date
import com.eyther.lumbridge.features.tasks.generated.resources.task_at_time
import com.eyther.lumbridge.features.tasks.generated.resources.task_notification_time
import com.eyther.lumbridge.features.tasks.generated.resources.task_notify_household
import com.eyther.lumbridge.features.tasks.generated.resources.task_picker_done
import com.eyther.lumbridge.features.tasks.generated.resources.task_remove_date
import com.eyther.lumbridge.features.tasks.generated.resources.ui_cancel
import com.eyther.lumbridge.features.tasks.generated.resources.ui_times_are_in_1_s
import com.eyther.lumbridge.features.tasks.presentation.mapper.taskRecurrenceLabel
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/**
 * Only picker drafts are transient here; confirmed schedule values belong to the Task ViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskScheduleFields(
    date: LocalDate,
    time: LocalTime,
    hasDueDate: Boolean,
    notificationEnabled: Boolean,
    recurrence: TaskRecurrenceDomain,
    busy: Boolean,
    timeZone: String,
    onDate: (LocalDate) -> Unit,
    onTime: (LocalTime) -> Unit,
    onDueDateEnabled: (Boolean) -> Unit,
    onNotificationEnabled: (Boolean) -> Unit,
    onRecurrence: (TaskRecurrenceDomain) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        OutlinedButton(
            onClick = { showDatePicker = true },
            enabled = !busy,
            modifier = Modifier.weight(1f).testTag("task_date"),
        ) {
            Text(
                if (hasDueDate) calendarDateText(date)
                else stringResource(Res.string.task_add_date),
            )
        }
        if (hasDueDate)
            DestructiveIconButton(
                description = stringResource(Res.string.task_remove_date),
                onClick = { onDueDateEnabled(false) },
                enabled = !busy,
            )
    }
    if (hasDueDate) {
        val notificationLabel = stringResource(Res.string.task_notify_household)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(notificationLabel, modifier = Modifier.weight(1f))
            Switch(
                checked = notificationEnabled,
                onCheckedChange = onNotificationEnabled,
                enabled = !busy,
                modifier =
                    Modifier.testTag("task_notification").semantics {
                        contentDescription = notificationLabel
                    },
            )
        }
        if (notificationEnabled) {
            OutlinedButton(
                onClick = { showTimePicker = true },
                enabled = !busy,
                modifier = Modifier.testTag("task_time"),
            ) {
                Text(stringResource(Res.string.task_at_time, time.toString()))
            }
            Text(
                stringResource(Res.string.ui_times_are_in_1_s, timeZone),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small)) {
            TaskRecurrenceDomain.entries.forEach { option ->
                FilterChip(
                    selected = recurrence == option,
                    onClick = { onRecurrence(option) },
                    label = { Text(taskRecurrenceLabel(option)) },
                    enabled = !busy,
                )
            }
        }
    }
    if (showDatePicker) {
        val pickerState =
            rememberDatePickerState(
                initialSelectedDateMillis = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                LoomButton(
                    text = stringResource(Res.string.task_picker_done),
                    enabled = pickerState.selectedDateMillis != null,
                    modifier = Modifier.testTag("confirm_task_date"),
                    onClick = {
                        pickerState.selectedDateMillis?.let { selected ->
                            onDate(
                                Instant.fromEpochMilliseconds(selected)
                                    .toLocalDateTime(TimeZone.UTC)
                                    .date,
                            )
                        }
                        showDatePicker = false
                    },
                )
            },
            dismissButton = {
                OutlinedButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.ui_cancel))
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
    if (showTimePicker) {
        val pickerState =
            rememberTimePickerState(
                initialHour = time.hour,
                initialMinute = time.minute,
                is24Hour = true,
            )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(Res.string.task_notification_time)) },
            text = { TimeInput(state = pickerState) },
            confirmButton = {
                LoomButton(
                    text = stringResource(Res.string.task_picker_done),
                    modifier = Modifier.testTag("confirm_task_time"),
                    onClick = {
                        onTime(LocalTime(hour = pickerState.hour, minute = pickerState.minute))
                        showTimePicker = false
                    },
                )
            },
            dismissButton = {
                OutlinedButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(Res.string.ui_cancel))
                }
            },
        )
    }
}
