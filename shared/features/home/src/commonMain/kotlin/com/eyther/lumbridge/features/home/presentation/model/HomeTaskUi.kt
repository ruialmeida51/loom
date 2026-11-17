package com.eyther.lumbridge.features.home.presentation.model

import com.eyther.lumbridge.features.tasks.presentation.model.TaskDateUi

data class HomeTaskUi(
    val id: String,
    val title: String,
    val date: TaskDateUi?,
)
