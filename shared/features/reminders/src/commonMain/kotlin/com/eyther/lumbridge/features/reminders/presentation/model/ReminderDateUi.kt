package com.eyther.lumbridge.features.reminders.presentation.model

import androidx.compose.runtime.Immutable

@Immutable
data class ReminderDateUi(
    val daysFromToday: Int,
    val day: Int,
    val month: Int,
    val year: Int?,
    val time: String,
)
