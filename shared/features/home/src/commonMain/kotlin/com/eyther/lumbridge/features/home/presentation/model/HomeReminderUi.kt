package com.eyther.lumbridge.features.home.presentation.model

import com.eyther.lumbridge.features.reminders.presentation.model.ReminderDateUi

data class HomeReminderUi(
    val id: String,
    val title: String,
    val date: ReminderDateUi,
)
