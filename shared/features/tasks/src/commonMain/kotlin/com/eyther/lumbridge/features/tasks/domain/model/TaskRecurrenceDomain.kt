package com.eyther.lumbridge.features.tasks.domain.model

import kotlinx.datetime.DatePeriod

enum class TaskRecurrenceDomain(val period: DatePeriod?) {
    NONE(null),
    DAILY(DatePeriod(days = 1)),
    WEEKLY(DatePeriod(days = 7)),
}
