package com.eyther.lumbridge.features.home.presentation.mapper

import com.eyther.lumbridge.features.home.presentation.model.HomeTaskUi
import com.eyther.lumbridge.features.tasks.domain.model.TaskDomain
import com.eyther.lumbridge.features.tasks.presentation.mapper.toDateUi
import kotlinx.datetime.LocalDate

internal fun TaskDomain.toHomeUi(today: String, timeZone: String): HomeTaskUi {
    return HomeTaskUi(
        id = id,
        title = title,
        date = toDateUi(today = LocalDate.parse(today), timeZone = timeZone),
    )
}
