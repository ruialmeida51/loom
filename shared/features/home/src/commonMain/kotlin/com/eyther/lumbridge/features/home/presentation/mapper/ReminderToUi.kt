package com.eyther.lumbridge.features.home.presentation.mapper

import com.eyther.lumbridge.features.home.presentation.model.HomeReminderUi
import com.eyther.lumbridge.features.reminders.domain.model.ReminderDomain
import com.eyther.lumbridge.features.reminders.presentation.mapper.toDateUi
import kotlinx.datetime.LocalDate

internal fun ReminderDomain.toHomeUi(today: String, timeZone: String): HomeReminderUi {
    return HomeReminderUi(
        id = id,
        title = title,
        date = toDateUi(today = LocalDate.parse(today), timeZone = timeZone),
    )
}
