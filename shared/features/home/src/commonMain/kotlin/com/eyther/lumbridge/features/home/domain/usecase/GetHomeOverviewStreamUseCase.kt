package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.getOrThrow
import com.eyther.lumbridge.core.domain.wrapper.safeCall
import com.eyther.lumbridge.features.home.domain.model.HomeOverviewDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class GetHomeOverviewStreamUseCase(
    private val getRemindersStreamUseCase: GetRemindersStreamUseCase,
    private val context: HouseholdContextDomain,
) {
    operator fun invoke(): Flow<AppResult<HomeOverviewDomain>> =
        getRemindersStreamUseCase().map { result ->
            safeCall {
                val today = todayIso(context.timeZone)
                val pending = result.getOrThrow().filterNot { it.completed }.sortedBy { it.dueAt }
                val (due, upcoming) =
                    pending.partition { reminder ->
                        Instant.parse(reminder.dueAt)
                            .toLocalDateTime(TimeZone.of(context.timeZone))
                            .date
                            .toString() <= today
                    }
                HomeOverviewDomain(today = today, reminders = due, upcomingReminders = upcoming)
            }
        }
}
