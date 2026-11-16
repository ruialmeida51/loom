package com.eyther.lumbridge.features.home.domain.usecase

import com.eyther.lumbridge.core.domain.todayIso
import com.eyther.lumbridge.core.domain.wrapper.AppResult
import com.eyther.lumbridge.core.domain.wrapper.mapToResult
import com.eyther.lumbridge.features.home.domain.model.HomeRemindersDomain
import com.eyther.lumbridge.features.household.domain.model.HouseholdContextDomain
import com.eyther.lumbridge.features.reminders.domain.usecase.GetRemindersStreamUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetHomeRemindersStreamUseCase(
    private val getRemindersStreamUseCase: GetRemindersStreamUseCase,
    private val context: HouseholdContextDomain,
) {
    operator fun invoke(): Flow<AppResult<HomeRemindersDomain>> =
        getRemindersStreamUseCase().map { result ->
            result.mapToResult { reminders ->
                HomeRemindersDomain(
                    today = todayIso(context.timeZone),
                    reminders = reminders.filterNot { it.completed }.sortedBy { it.dueAt },
                )
            }
        }
}
