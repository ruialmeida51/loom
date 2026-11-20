package com.eyther.lumbridge.features.tasks.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/** When open tasks are due, in the order the list shows them. */
enum class TaskGroupKindUi {
    OVERDUE,
    TODAY,
    TOMORROW,
    LATER,
    ANYTIME,
}

@Immutable data class TaskGroupUi(val kind: TaskGroupKindUi, val rows: ImmutableList<TaskRowUi>)
