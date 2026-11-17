package com.eyther.lumbridge.features.tasks.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.DestructiveIconButton
import com.eyther.lumbridge.core.presentation.theme.LoomColors
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.task_delete_description
import com.eyther.lumbridge.features.tasks.generated.resources.ui_repeats_1_s
import com.eyther.lumbridge.features.tasks.presentation.mapper.taskRecurrenceLabel
import com.eyther.lumbridge.features.tasks.presentation.model.TaskRowUi
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TaskListRow(
    row: TaskRowUi,
    completing: Boolean,
    removing: Boolean,
    onComplete: (TaskDomain) -> Unit,
    onRemove: (String) -> Unit,
) {
    val task = row.task
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .toggleable(
                    value = task.completed,
                    enabled = !completing && !removing,
                    role = Role.Checkbox,
                    onValueChange = { onComplete(task) },
                )
                .testTag("complete_${task.id}")
                .semantics { contentDescription = task.title }
                .padding(vertical = LoomSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
    ) {
        Box(modifier = Modifier.size(TaskCheckboxSlot), contentAlignment = Alignment.Center) {
            if (completing)
                CircularProgressIndicator(Modifier.size(TaskProgressSize), strokeWidth = 2.dp)
            else
                Checkbox(
                    checked = task.completed,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(checkedColor = LoomColors.positive),
                )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.completed) TextDecoration.LineThrough else null,
                color =
                    if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurface,
            )
            row.date?.let { TaskDueDate(date = it) }
            if (task.recurrence != TaskRecurrenceDomain.NONE)
                Text(
                    stringResource(Res.string.ui_repeats_1_s, taskRecurrenceLabel(task.recurrence)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
        }
        DestructiveIconButton(
            description = stringResource(Res.string.task_delete_description, task.title),
            onClick = { onRemove(task.id) },
            enabled = !completing,
            loading = removing,
        )
    }
}

// Reserve the same leading geometry while a completion request is running.
private val TaskCheckboxSlot = 48.dp
private val TaskProgressSize = 24.dp
