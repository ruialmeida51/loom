package com.eyther.lumbridge.features.tasks.presentation.model

import androidx.compose.runtime.Immutable
import com.eyther.lumbridge.features.household.domain.model.MemberDomain
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain

/**
 * A task as the list shows it: its date, whose job it is ([assignee], or a former member when
 * [assigneeMissing]) and, for turns, who goes [next].
 */
@Immutable
data class TaskRowUi(
    val task: TaskDomain,
    val date: TaskDateUi?,
    val assignee: MemberDomain? = null,
    val assigneeMissing: Boolean = false,
    val next: MemberDomain? = null,
    val isMine: Boolean = false,
)
