package com.eyther.lumbridge.features.home.presentation.model

data class HomeReminderUi(
    val id: String,
    val title: String,
    val due: String,
    val overdue: Boolean = false,
)
