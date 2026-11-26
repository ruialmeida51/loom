package com.eyther.lumbridge.features.tasks.presentation.components.bottomsheet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.eyther.lumbridge.core.presentation.components.bottomsheet.FormBottomSheet
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskRecurrenceDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.task_assign_title
import com.eyther.lumbridge.features.tasks.presentation.components.TaskPeopleFields
import org.jetbrains.compose.resources.stringResource

/** Changes who does an existing task, and whether everyone takes turns at it. */
@Composable
fun TaskAssignBottomSheet(
    task: TaskDomain,
    members: List<MemberDomain>,
    userId: String,
    assigneeId: String?,
    takesTurns: Boolean,
    canTakeTurns: Boolean,
    busy: Boolean,
    onAssignee: (String?) -> Unit,
    onTakesTurns: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    FormBottomSheet(
        title = stringResource(Res.string.task_assign_title),
        onAction = onSave,
        onDismiss = onDismiss,
        busy = busy,
        actionModifier = Modifier.testTag("save_task_assignee"),
    ) {
        TaskPeopleFields(
            members = members,
            userId = userId,
            assigneeId = assigneeId,
            takesTurns = takesTurns,
            canTakeTurns = canTakeTurns,
            repeats = task.recurrence != TaskRecurrenceDomain.NONE,
            enabled = !busy,
            onAssignee = onAssignee,
            onTakesTurns = onTakesTurns,
        )
    }
}
