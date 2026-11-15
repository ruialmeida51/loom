package com.eyther.lumbridge.features.home.presentation.viewmodel.contracts

import com.eyther.lumbridge.core.presentation.resources.TextResource
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.home.presentation.mapper.toHomeUi
import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi

data class HomeState(
    val overview: HomeOverviewDomain? = null,
    val loading: Boolean = true,
    val timeZone: String = "UTC",
    val error: TextResource? = null,
) {
    val hasOverview: Boolean = overview != null
    val reminders: List<HomeReminderUi> =
        overview?.reminders.orEmpty().take(3).map {
            it.toHomeUi(today = overview?.today.orEmpty(), timeZone = timeZone)
        }
    val upcomingReminders: List<HomeReminderUi> =
        overview?.upcomingReminders.orEmpty().take(2).map {
            it.toHomeUi(today = overview?.today.orEmpty(), timeZone = timeZone)
        }
}
