package com.eyther.lumbridge.features.tasks.presentation.model

import androidx.compose.runtime.Immutable
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain

@Immutable data class TaskRowUi(val task: TaskDomain, val date: TaskDateUi?)
