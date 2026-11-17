package com.eyther.lumbridge.features.home.domain.model

import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain

/** The household date is refreshed with the tasks, including after a lifecycle restart. */
data class HomeTasksDomain(val today: String, val tasks: List<TaskDomain>)
