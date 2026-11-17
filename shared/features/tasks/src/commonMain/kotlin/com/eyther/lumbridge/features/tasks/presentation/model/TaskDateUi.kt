package com.eyther.lumbridge.features.tasks.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class TaskDateUi(
    val daysFromToday: Int,
    val day: Int,
    val month: Int,
    val year: Int?,
    val time: String?,
)
