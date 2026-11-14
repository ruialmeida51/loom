package com.eyther.lumbridge.features.reminders.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CompleteReminderRequest(
    @SerialName("p_reminder_id") val reminderId: String,
    @SerialName("p_completed") val completed: Boolean,
    @SerialName("p_expected_due_at") val expectedDueAt: String,
)
