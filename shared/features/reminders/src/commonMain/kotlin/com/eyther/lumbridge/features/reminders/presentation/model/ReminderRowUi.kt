package com.eyther.lumbridge.features.reminders.presentation.model

import androidx.compose.runtime.Immutable
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain

@Immutable data class ReminderRowUi(val reminder: ReminderDomain, val date: ReminderDateUi)
