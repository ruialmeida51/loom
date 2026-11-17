package com.eyther.lumbridge.features.tasks.data.network.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CompleteTaskRequest(
    @SerialName("p_task_id") val taskId: String,
    @SerialName("p_completed") val completed: Boolean,
    @SerialName("p_expected_due_at") val expectedDueAt: String?,
)
