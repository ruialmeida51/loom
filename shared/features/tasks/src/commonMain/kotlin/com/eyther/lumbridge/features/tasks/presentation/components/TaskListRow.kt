package com.eyther.lumbridge.features.tasks.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.ActionProgress
import com.eyther.lumbridge.core.presentation.components.CheckCircle
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.components.cardSegment
import com.eyther.lumbridge.core.presentation.components.menu.ActionMenu
import com.eyther.lumbridge.core.presentation.components.menu.model.ActionMenuItem
import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.task_assign_action
import com.eyther.lumbridge.features.tasks.generated.resources.task_assigned_to
import com.eyther.lumbridge.features.tasks.generated.resources.task_delete_action
import com.eyther.lumbridge.features.tasks.generated.resources.task_former_member
import com.eyther.lumbridge.features.tasks.generated.resources.task_options
import com.eyther.lumbridge.features.tasks.generated.resources.task_turn_then
import com.eyther.lumbridge.features.tasks.generated.resources.task_you
import com.eyther.lumbridge.features.tasks.generated.resources.task_your_turn_then
import com.eyther.lumbridge.features.tasks.generated.resources.ui_repeats_1_s
import com.eyther.lumbridge.features.tasks.presentation.mapper.taskRecurrenceLabel
import com.eyther.lumbridge.features.tasks.presentation.model.TaskRowUi
import org.jetbrains.compose.resources.stringResource

/**
 * One row of a grouped task card. The whole row toggles completion; deleting lives in the row's
 * overflow menu, which opens the confirmation. [first], [last] and [gap] join rows into one card.
 */
@Composable
internal fun TaskListRow(
    row: TaskRowUi,
    completing: Boolean,
    removing: Boolean,
    onComplete: (TaskDomain) -> Unit,
    onRemove: (String) -> Unit,
    onAssign: (TaskDomain) -> Unit = {},
    showsPeople: Boolean = false,
    userId: String = "",
    first: Boolean = true,
    last: Boolean = true,
    gap: Dp = 0.dp,
) {
    val task = row.task
    Row(
        modifier =
            Modifier.fillMaxWidth()
                .cardSegment(first = first, last = last, gap = gap)
                .heightIn(min = TaskRowMinimumHeight)
                .padding(start = LoomSpacing.extraSmall, end = LoomSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier.weight(1f)
                    .toggleable(
                        value = task.completed,
                        enabled = !completing && !removing,
                        role = Role.Checkbox,
                        onValueChange = { onComplete(task) },
                    )
                    .testTag("complete_${task.id}")
                    .semantics { contentDescription = task.title }
                    .padding(vertical = LoomSpacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            Box(modifier = Modifier.size(TaskCheckSlot), contentAlignment = Alignment.Center) {
                if (completing)
                    ActionProgress(size = TaskProgressSize)
                else CheckCircle(checked = task.completed, onCheckedChange = null, muted = false)
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
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
                if (showsPeople) TaskPeopleLine(row = row, userId = userId)
                if (task.recurrence != TaskRecurrenceDomain.NONE)
                    Text(
                        stringResource(
                            Res.string.ui_repeats_1_s,
                            taskRecurrenceLabel(task.recurrence),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
            }
        }
        if (removing)
            Box(Modifier.size(TaskCheckSlot), contentAlignment = Alignment.Center) {
                ActionProgress(size = TaskProgressSize)
            }
        else
            ActionMenu(
                contentDescription = stringResource(Res.string.task_options, task.title),
                modifier = Modifier.testTag("task_actions_${task.id}"),
                items =
                    listOfNotNull(
                        ActionMenuItem(
                                text = TextResource.Resource(Res.string.task_assign_action),
                                onClick = { onAssign(task) },
                                enabled = !completing && !task.completed,
                            )
                            .takeIf { showsPeople },
                        ActionMenuItem(
                            text = TextResource.Resource(Res.string.task_delete_action),
                            onClick = { onRemove(task.id) },
                            enabled = !completing,
                            destructive = true,
                        ),
                    ),
            )
    }
}

/** Whose job it is, and for turns who goes next, beside their avatar. */
@Composable
private fun TaskPeopleLine(row: TaskRowUi, userId: String) {
    val assignee = row.assignee
    if (assignee == null && !row.assigneeMissing) return
    val you = stringResource(Res.string.task_you)
    val name =
        when {
            assignee == null -> stringResource(Res.string.task_former_member)
            assignee.id == userId -> you
            else -> assignee.name
        }
    val next = row.next?.let { if (it.id == userId) you else it.name }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.testTag("task_people_${row.task.id}"),
    ) {
        if (assignee != null)
            MemberAvatar(
                memberId = assignee.id,
                name = assignee.name,
                picturePath = assignee.avatarPath,
                modifier = Modifier.size(18.dp),
            )
        Text(
            text =
                when {
                    next == null -> stringResource(Res.string.task_assigned_to, name)
                    assignee?.id == userId -> stringResource(Res.string.task_your_turn_then, next)
                    else -> stringResource(Res.string.task_turn_then, name, next)
                },
            style = MaterialTheme.typography.labelMedium,
            color =
                if (row.isMine) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// Reserve the same leading geometry while a completion request is running.
private val TaskCheckSlot = 48.dp
private val TaskProgressSize = 22.dp
private val TaskRowMinimumHeight = 60.dp
