package com.eyther.lumbridge.features.tasks.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.eyther.lumbridge.core.presentation.components.MemberAvatar
import com.eyther.lumbridge.core.presentation.theme.LoomSpacing
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.tasks.generated.resources.Res
import com.eyther.lumbridge.features.tasks.generated.resources.task_anyone
import com.eyther.lumbridge.features.tasks.generated.resources.task_take_turns
import com.eyther.lumbridge.features.tasks.generated.resources.task_take_turns_hint
import com.eyther.lumbridge.features.tasks.generated.resources.task_take_turns_needs_person
import com.eyther.lumbridge.features.tasks.generated.resources.task_take_turns_needs_repeat
import com.eyther.lumbridge.features.tasks.generated.resources.task_who
import com.eyther.lumbridge.features.tasks.generated.resources.task_you
import org.jetbrains.compose.resources.stringResource

/**
 * Who does a task: anyone, or one person; repeating tasks can rotate through everyone, starting
 * with that person. [canTakeTurns] is false until the task repeats and someone is chosen.
 */
@Composable
fun TaskPeopleFields(
    members: List<MemberDomain>,
    userId: String,
    assigneeId: String?,
    takesTurns: Boolean,
    canTakeTurns: Boolean,
    repeats: Boolean,
    enabled: Boolean,
    onAssignee: (String?) -> Unit,
    onTakesTurns: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(stringResource(Res.string.task_who), style = MaterialTheme.typography.titleSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(LoomSpacing.small),
            verticalArrangement = Arrangement.spacedBy(LoomSpacing.extraSmall),
        ) {
            FilterChip(
                selected = assigneeId == null,
                onClick = { onAssignee(null) },
                enabled = enabled,
                label = { Text(stringResource(Res.string.task_anyone)) },
                modifier = Modifier.testTag("task_assignee_anyone"),
            )
            members.forEach { member ->
                FilterChip(
                    selected = assigneeId == member.id,
                    onClick = { onAssignee(member.id) },
                    enabled = enabled,
                    leadingIcon = {
                        MemberAvatar(
                            memberId = member.id,
                            initials = member.name.trim().take(1).uppercase(),
                            picturePath = member.avatarPath,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    label = {
                        Text(
                            if (member.id == userId) stringResource(Res.string.task_you)
                            else member.name,
                        )
                    },
                    modifier = Modifier.testTag("task_assignee_${member.name}"),
                )
            }
        }
        val assignee = members.firstOrNull { it.id == assigneeId }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(Res.string.task_take_turns))
                Text(
                    when {
                        !repeats -> stringResource(Res.string.task_take_turns_needs_repeat)
                        assignee != null ->
                            stringResource(
                                Res.string.task_take_turns_hint,
                                if (assignee.id == userId) stringResource(Res.string.task_you)
                                else assignee.name,
                            )
                        else -> stringResource(Res.string.task_take_turns_needs_person)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = takesTurns && canTakeTurns,
                onCheckedChange = onTakesTurns,
                enabled = enabled && canTakeTurns,
                modifier = Modifier.testTag("task_take_turns"),
            )
        }
    }
}
