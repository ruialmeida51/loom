package com.eyther.lumbridge.features.tasks.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AssignTaskRequest(
    @SerialName("assignee_id") val assigneeId: String?,
    @SerialName("rotation") val rotation: List<String>,
)
