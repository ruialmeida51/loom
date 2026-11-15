package com.eyther.lumbridge.features.home.domain.model

import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain

data class HomeOverviewDomain(
    val today: String = "",
    val reminders: List<ReminderDomain>,
    val upcomingReminders: List<ReminderDomain> = emptyList(),
)
