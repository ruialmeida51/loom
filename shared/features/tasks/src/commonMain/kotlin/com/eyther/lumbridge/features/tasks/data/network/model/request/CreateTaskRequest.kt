package com.eyther.lumbridge.features.tasks.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateTaskRequest(
    @SerialName("id") val id: String,
    @SerialName("household_id") val householdId: String,
    @SerialName("title") val title: String,
    @SerialName("due_at") val dueAt: String?,
    @SerialName("recurrence") val recurrence: String,
    @SerialName("completed") val completed: Boolean,
    @SerialName("created_by") val createdBy: String,
    @SerialName("notification_enabled") val notificationEnabled: Boolean,
    @SerialName("assignee_id") val assigneeId: String?,
    @SerialName("rotation") val rotation: List<String>,
)
