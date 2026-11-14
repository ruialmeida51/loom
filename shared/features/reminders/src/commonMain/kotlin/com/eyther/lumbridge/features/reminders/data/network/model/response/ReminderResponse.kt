package com.eyther.lumbridge.features.reminders.data.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReminderResponse(
    @SerialName("id") val id: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("title") val title: String,
    @SerialName("due_at") val dueAt: String,
    @SerialName("recurrence") val recurrence: String,
    @SerialName("completed") val completed: Boolean,
    @SerialName("created_by") val createdBy: String,
)
