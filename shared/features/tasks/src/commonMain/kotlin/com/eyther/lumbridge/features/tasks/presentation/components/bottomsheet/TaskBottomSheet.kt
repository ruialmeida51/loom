package com.eyther.lumbridge.features.tasks.presentation.components.bottomsheet

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.ErrorNotice
import com.eyther.lumbridge.core.presentation.components.LoomButton
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.ui_a_little_task
import com.eyther.lumbridge.features.tasks.generated.resources.ui_cancel
import com.eyther.lumbridge.features.tasks.generated.resources.ui_save_task
import com.eyther.lumbridge.features.tasks.generated.resources.ui_what_needs_doing
import com.eyther.lumbridge.features.tasks.presentation.components.TaskScheduleFields
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.stringResource

/** Title-first task form. The Host owns dismissal; confirmed inputs belong to the ViewModel. */
@Composable
fun TaskBottomSheet(
    title: String,
    date: LocalDate,
    time: LocalTime,
    hasDueDate: Boolean,
    notificationEnabled: Boolean,
    recurrence: TaskRecurrenceDomain,
    busy: Boolean,
    formError: TextResource?,
    timeZone: String,
    onShowAdd: (Boolean) -> Unit,
    onTitle: (String) -> Unit,
    onDate: (LocalDate) -> Unit,
    onTime: (LocalTime) -> Unit,
    onDueDateEnabled: (Boolean) -> Unit,
    onNotificationEnabled: (Boolean) -> Unit,
    onRecurrence: (TaskRecurrenceDomain) -> Unit,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.ui_a_little_task),
        action = {
            LoomButton(
                text = stringResource(Res.string.ui_save_task),
                onClick = onAdd,
                loading = busy,
                modifier = Modifier.testTag("save_task"),
            )
        },
        dismissAction = {
            OutlinedButton(onClick = { onShowAdd(false) }, enabled = !busy) {
                Text(stringResource(Res.string.ui_cancel))
            }
        },
    ) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitle,
            label = { Text(stringResource(Res.string.ui_what_needs_doing)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("task_title"),
            enabled = !busy,
        )
        TaskScheduleFields(
            date = date,
            time = time,
            hasDueDate = hasDueDate,
            notificationEnabled = notificationEnabled,
            recurrence = recurrence,
            busy = busy,
            timeZone = timeZone,
            onDate = onDate,
            onTime = onTime,
            onDueDateEnabled = onDueDateEnabled,
            onNotificationEnabled = onNotificationEnabled,
            onRecurrence = onRecurrence,
        )
        formError?.let { message -> ErrorNotice(message = message, onDismiss = onDismiss) }
    }
}
