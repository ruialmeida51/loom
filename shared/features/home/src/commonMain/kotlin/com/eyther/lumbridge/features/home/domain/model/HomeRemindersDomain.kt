package com.eyther.lumbridge.features.home.domain.model

import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain

/** The household date is refreshed with the reminders, including after a lifecycle restart. */
data class HomeRemindersDomain(val today: String, val reminders: List<ReminderDomain>)
